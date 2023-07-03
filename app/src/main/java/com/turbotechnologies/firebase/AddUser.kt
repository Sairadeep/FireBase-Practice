package com.turbotechnologies.firebase

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.turbotechnologies.firebase.databinding.ActivityAddUserBinding
import java.util.*

class AddUser : AppCompatActivity() {
    lateinit var addUserBinding: ActivityAddUserBinding

    // Creating an instance of the FireBase DB.
    val dataBase: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Creating an object from the FireBase storage Reference class
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    // Creating a reference from the Storage reference
    val storageReference: StorageReference = firebaseStorage.reference

    // Creating a reference for the database using the above created object.
    val myReferenceForAddUser: DatabaseReference = dataBase.reference.child("Employees")
    // As you want to create a new child which doesn't exists, then reference to it > dataBase.reference.child("New Child Name")

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var imageUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addUserBinding = ActivityAddUserBinding.inflate(layoutInflater)
        val view = addUserBinding.root
        setContentView(view)

        // Setting the name of the action bar
        supportActionBar?.title = "Add User"

        // Register the activityResultLauncher object otherwise the application is not going to work
        registerActivityForResult()

        addUserBinding.buttonAddUser.setOnClickListener {
            uploadPhoto()
        }
        addUserBinding.imageViewDP.setOnClickListener {
            // Selecting the image from the devices memory
            chooseImage()
        }
    }

    // Method to add a user to Real Time DB
    private fun addUserToDB(url: String,userImage:String) {
        // First take the data that the user entered.
        val name: String = addUserBinding.editTextAddUserName.text.toString()
        val email: String = addUserBinding.edittextAddEmail.text.toString()
        val age: Int = addUserBinding.editTextAddAge.text.toString().toInt()
        // We need to create a unique key for each user
        val id: String = myReferenceForAddUser.push().key as String
        // push() => Will create a "Unique Key" for each user that will be added under the 'NewEmployee' child
        // And to take the unique key -> ".key"

        // Create an object from the users data class
        val users = Users(id, name, email, age, url, userImage)
        // Adding the user object to the DB and we need to register each user under its unique id, otherwise there will be only one user in DB where every time it will overwrite the existing user.
        // We can actually observe whether the data is added to DB or not i.e., the right operation to DB is successful or not ===> Add "Listeners".
        // CancelledListener, CompleteListener, SuccessListener, FailureListener
        myReferenceForAddUser.child(id).setValue(users).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If task is successful, show a toast message
                Toast.makeText(
                    applicationContext,
                    "User has been successfully added to the DB",
                    Toast.LENGTH_LONG
                ).show()
                // Clicking the upload button only once.
                addUserBinding.buttonAddUser.isClickable = true
                // Visibility of the progress bar.
                addUserBinding.progressBarImageUpload.visibility = View.INVISIBLE
                finish()
            } else {
                // If the task isn't successful, find out the cause of error and display a toast message as a reason.
                Toast.makeText(
                    applicationContext,
                    task.exception.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun chooseImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Parameter 1 -> Context, Parameter 2 -> permission
            // PackageManager,PERMISSION_GRANTED -> This will ask the user for the second permission.
            // And if the user doesn't allow it, there won't be any further action.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
            // Parameter 1 -> Context, Parameter 3 -> request code
            // Parameter 2 -> Array and within this we will specify the permissions that we need to obtain from the user i.e., Requesting one or more permissions at a time.
        } else {
            // Though the user has provided all the permissions while downloading an app. They can then ask for conformation from the users in the situations requiring higher security like reading external storage
            // If the user allows, this code will work
            // Opening the device gallery and the image selection process is done by the "Intent" class
            val intent = Intent()
            intent.type =
                "image/*"      // '.type' specifies the type of file to be shown to the user., for images uses "images/*"
            intent.action = Intent.ACTION_GET_CONTENT // This opens the devices file system to choose from.
            // After choosing the image, we need to take this image and display in the image view component -> Hence, start the intent with ActivityResultLauncher
            activityResultLauncher.launch(intent)
        }
    }

    // It is a callback method in Android that is called when the user responds to a permission request.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // The User granted the permission. Hence, open the device gallery and perform the image selection operation again.
            val intent = Intent()
            intent.type =
                "image/*"          // '.type' specifies the type of file to be shown to the user., for images uses "images/*"
            intent.action =
                Intent.ACTION_GET_CONTENT // This opens the devices file system to choose from.
            // After choosing the image, we need to take this image and display in the image view component -> Hence, start the intent with ActivityResultLauncher
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerActivityForResult() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
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
                            Picasso.get().load(it).into(addUserBinding.imageViewDP)
                        }
                    }
                })
    }

    fun uploadPhoto() {
        // Clicking the upload button only once.
        addUserBinding.buttonAddUser.isClickable = false
        // Separate name for each user profile that can be achieved using UUID.
        val imageName = UUID.randomUUID().toString()
        // Visibility of the progress bar.
        addUserBinding.progressBarImageUpload.visibility = View.VISIBLE
        // Creating an other object in the storage reference class, images need to be stored in a separate folder i.e., the name specified in the child section
        val imageReference = storageReference.child("images").child(imageName)
        // putFile() -> We specify the path of the image to be uploaded. And as the imageUri can be null, use "let"
        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Image uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // After the image uploaded, take the URL and save it to RTDB.
                val myUploadedImageReference = storageReference.child("images").child(imageName)
                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->
                    // Pass the Downloaded URL to a different variable using the url
                    val imageURL = url.toString()
                    // Saving this URL to RTDB
                    addUserToDB(imageURL,imageName)
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}