package com.turbotechnologies.firebase

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.turbotechnologies.firebase.databinding.ActivityUpdateUserBinding
import java.util.*

class UpdateUser : AppCompatActivity() {
    lateinit var updateUserBinding: ActivityUpdateUserBinding
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val updateReference: DatabaseReference = database.reference.child("Employees")
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var imageUri: Uri? = null
    // Creating an object from the FireBase storage Reference class
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    // Creating a reference from the Storage reference
    val storageReference: StorageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateUserBinding = ActivityUpdateUserBinding.inflate(layoutInflater)
        val view = updateUserBinding.root
        setContentView(view)
        supportActionBar?.title = "Update User"
        // Register the activityResultLauncher object otherwise the application is not going to work
        registerActivityForResult()
        getAndSetData()
        // Update the data when the user clicks on the 'Update User' button
        updateUserBinding.buttonUpdateUser.setOnClickListener {
           // Once the user clicks on the "Update User", first the image is uploaded to cloud storage and then the data is updated to DB.
            uploadPhoto()
        }
        updateUserBinding.imageViewDPupdate.setOnClickListener {
            // Selecting the image from the devices memory
            chooseImage()
        }
    }

    // Getting the data from the intent via the UserAdapter class
    @SuppressLint("SetTextI18n")
    fun getAndSetData() {
        // Getting the data
        val name = intent.getStringExtra("name")
        val age = intent.getIntExtra("age", 0).toString()
        val email = intent.getStringExtra("email")
        val imageUrl = intent.getStringExtra("imageUrl").toString()

        // Setting the data
        updateUserBinding.editTextUpdateUserName.setText(name)
        updateUserBinding.edittextUpdateEmail.setText(email)
        updateUserBinding.editTextUpdateAge.setText(age)
        Picasso.get().load(imageUrl).into(updateUserBinding.imageViewDPupdate)
    }

    private fun chooseImage() {
        // As the user already provided the permission. Hence, no need to check for permission while updating.
            // Opening the device gallery and the image selection process is done by the "Intent" class. This code is helpful for opening the gallery.
            val intent = Intent()
            intent.type =
                "image/*"      // '.type' specifies the type of file to be shown to the user., for images uses "images/*"
            intent.action = Intent.ACTION_GET_CONTENT // This opens the devices file system to choose from.
            // After choosing the image, we need to take this image and display in the image view component -> Hence, start the intent with ActivityResultLauncher
            activityResultLauncher.launch(intent)
    }

    // Method for updating the data
    private fun updateData(imageUrl:String, imageName:String) {
        // Get the data from the user
        val updatedName = updateUserBinding.editTextUpdateUserName.text.toString()
        val updatedAge = updateUserBinding.editTextUpdateAge.text.toString().toInt()
        val updatedEmail = updateUserBinding.edittextUpdateEmail.text.toString()
        val userID = intent.getStringExtra("id").toString()

        // Data in the FireBase RTDB is placed in "Key-value" pairs. Hence, transfer the data to the variable using the MAP class
        // Creating a variable of map class
        val userMap = mutableMapOf<String, Any>()
        // keyword should be same as the names that you specified in the Users modal class. Key is placed in the parenthesis
        // Update the DB
        userMap["userID"] = userID
        userMap["userName"] = updatedName
        userMap["userAge"] = updatedAge
        userMap["userEmail"] = updatedEmail
        userMap["url"] = imageUrl
        userMap["userImage"] = imageName

        // Now we can access the "Employee" child using the object "updateReference" and update the data
        updateReference.child(userID).updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "Data has been successfully updated",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                // After successful updation, make the progress bar invisible and enable the update user button.
                updateUserBinding.buttonUpdateUser.isClickable = true
                updateUserBinding.progressBarImageUpdate.visibility = View.INVISIBLE
            }
        }
    }

    private fun registerActivityForResult() {
        activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback {
                    // Getting the return result from the ActivityResultCallback and capture the result using the 'it' keyword or with lambda
                        result ->
                    // Determine the image selected by the user inside the activity call back, getting the result from above intent
                    val resultCode = result.resultCode
                    val imageData = result.data
                    // Checking the data
                    if (resultCode == RESULT_OK && imageData != null) {
                        // Find the path of the user selected image and assign it to a container. Hence, create an object from the URI class
                        // Assigning the path
                        imageUri = imageData.data
                        // Add a library to the project in order to show the image in the image view using the imageUri object
                        // As the imageUri is nullable & into() is used to determine where the image to be displayed.
                        imageUri.let {
                            Picasso.get().load(it).into(updateUserBinding.imageViewDPupdate)
                        }
                    }
                })
    }

    fun uploadPhoto() {
        // Clicking the upload button only once.
        updateUserBinding.buttonUpdateUser.isClickable = false
        // Getting the imageName from intent
        val imageName = intent.getStringExtra("imageName").toString()
        // Visibility of the progress bar.
        updateUserBinding.progressBarImageUpdate.visibility = View.VISIBLE
        // Creating an other object in the storage reference class, images need to be stored in a separate folder i.e., the name specified in the child section
        val imageReference = storageReference.child("images").child(imageName)
        // putFile() -> We specify the path of the image to be uploaded. And as the imageUri can be null, use "let"
        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Image updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // After the image uploaded, take the URL and save it to RTDB.
                val myUploadedImageReference = storageReference.child("images").child(imageName)
                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->
                    // Pass the Downloaded URL to a different variable using the url
                    val imageURL = url.toString()
                    // Saving this URL to RTDB
                    updateData(imageURL,imageName)
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}