package com.turbotechnologies.firebase

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.turbotechnologies.firebase.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val dataRetriveReference: DatabaseReference = database.reference.child("Employees")
    val firebaseStorage: FirebaseStorage  = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference

    // Send the data from what we retrieved from the DB to the adapter class and display the data in the recycler view.
    // Create an arraylist of modal class
    val userList = ArrayList<Users>()

    // Create an arraylist for all users images
    val imageNameList = ArrayList<String>()

    // Creating an object from adapter class
    lateinit var userAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        mainBinding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddUser::class.java)
            startActivity(intent)
        }
        retrieveDataFromDB()

        // To swipe left or right use the ItemTouchHelper class
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                // Drag and drop ie., Select an item, and drag it to the trash
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            @SuppressLint("SuspiciousIndentation")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // viewHolder.adapterPosition > Will determine which element to be deleted based on the position of the item
                val id = userAdapter.getUserId(viewHolder.adapterPosition)
                // Delete the user with this id using the method "removeValue()"
                dataRetriveReference.child(id).removeValue()
                // On deleting a user from RTDB, user related files should be deleted from the cloud storage as well
                // Call delete() after determining the file path that we want to delete
                val imageName =
                    userAdapter.getImageName(viewHolder.adapterPosition)  // We will get the name of image to be deleted.
                // Create a reference for the image that will be deleted -> Create at global scope and also create an new variable that references to the image to be deleted
                val imageReference = storageReference.child("images").child(imageName)
                // Now, the imageReference objects represents the image to be deleted.
                imageReference.delete()

                Toast.makeText(applicationContext, "Data has been removed", Toast.LENGTH_SHORT)
                    .show()
            }
        }).attachToRecyclerView(mainBinding.RecyclerView)
        // Finally connect the ItemTouchHelper to the recycler view as removed data should not be shown in the Recycler view
    }


    // Retrieving data from DB
    private fun retrieveDataFromDB() {
        dataRetriveReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // We should delete the data every time before we read the data otherwise same data will be called multiple times
                // We can use "ChildEventListener" as it has many functions like "OnChildAdded", "OnChildRemoved", "OnChildChanged" etc or we can also clear the arraylist.
                userList.clear()
                // Get the data from the DB using the for each loop so that we can all the data from the DB one after the other.
                for (eachUser in snapshot.children) {
                    val user = eachUser.getValue(Users::class.java)
                    // Getting the data and print it in logcat
                    if (user != null) {
                        println("userID: ${user.userID}")
                        println("userName: ${user.userName}")
                        println("userEmail: ${user.userEmail}")
                        println("userAge: ${user.userAge}")
                        println("********************************")

                        // Transferring the data to the arraylist and all the data in DB is transferred to the arraylist as an object of the users class
                        userList.add(user)
                    }

                    // Sending this data from the arraylist to the adapter class
                    userAdapter = UsersAdapter(this@MainActivity, userList)

                    // Define this adapter to the recycler view
                    // Specifying how the data is displayed
                    mainBinding.RecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    // Adding the adapter to the Recycler view
                    mainBinding.RecyclerView.adapter = userAdapter
                }
                invalidateOptionsMenu()
            }

            override fun onCancelled(error: DatabaseError) {
                // Action to be taken when the data cannot be retrieved or an error occurs.
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Linking the menu in the mainActivity
        menuInflater.inflate(
            // Parameter 1 -> Path of the menu design
            // Parameter 2 -> menu object
            R.menu.menu_items,
            menu
        )
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val deleteItem = menu?.findItem(R.id.deleteall)
        deleteItem?.isVisible = userList.isNotEmpty()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Necessary action to be done when the trash icon is selected
        if (item.itemId == R.id.deleteall) {
            // Display a dialog message
            showDialog()
        }
        // Selecting the logout item, performs logout
        else if (item.itemId == R.id.signOut) {
            // Log out the user from the app
            FirebaseAuth.getInstance()
                .signOut() // Now the user will exit from FireBase and also from the app
            // Navigating the user to the login page after sign out
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // To close the current activity i.e., the main activity.
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showDialog() {
        val dialogMessage = AlertDialog.Builder(this)
        dialogMessage.setTitle("Delete All Users?")
            .setMessage("Clicking on 'Yes' will delete all the data and can't be retrieved back and if you choose to delete a single data, please swipe to left or right for that particular data")
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })
        dialogMessage.setPositiveButton(
            "Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->
                // We can't directly delete all the images by deleting the image folder from cloud storage. Hence, take the names of all the images and transfer them to an array list
                // Getting the data from add value event listener
                dataRetriveReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (eachUser in snapshot.children) {
                            val user = eachUser.getValue(Users::class.java)
                            // Getting the data related to imageName and from it to an arraylist
                            if (user != null) {
                                // All the data in DB can be accessible using the user variable
                                imageNameList.add(user.userImage)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
                // Delete all users using the reference object
                dataRetriveReference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Notify the adapter about the changes because data is completed deleted
                        userAdapter.notifyDataSetChanged()
                        // After knowing that the data in RTDB is deleted successfully, now delete its corresponding images from cloud storage
                        for (userImage in imageNameList) {
                            // Create a reference to delete files by  removing the files inside the folder one after the other from cloud storage as we can't delete a folder directly
                            val imageReference = storageReference.child("images").child(userImage)
                            imageReference.delete()
                        }
                        Toast.makeText(
                            applicationContext,
                            "Successfully deleted everything",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        dialogMessage.setCancelable(false)
        dialogMessage.create().show()
    }
}