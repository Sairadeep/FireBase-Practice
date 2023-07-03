package com.turbotechnologies.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.turbotechnologies.firebase.databinding.UsersItemsBinding
import java.lang.Exception

class UsersAdapter(var context: Context, var users: ArrayList<Users>) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    // We will be sending the data that we created in the main activity to the adapter class
    // context : Context -> We use the intent class as it opens a new activity on selecting a data and we need a context parameter
    // that contains the activity properties as we are not in a activity.
    // Parameter 2 > arrayList of users modal class

    // Create an inner class to represent a users item design and define the components
    inner class UsersViewHolder(val adapterBinding: UsersItemsBinding) :
        RecyclerView.ViewHolder(adapterBinding.root) {}
    // Create a binding object as a constructor and as we specified "adapterBinding.root" >> We can directly access the components (TextView components).

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding = UsersItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
            // Parameter 1 -> LayoutInflater.from(parent.context)
        )
        return UsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        // Transfer the data in the arrays to the components available in the activity.
        // As it is not a activity, we cannot directly access the components here >> Use holder object from 'UsersViewHolder' class
        holder.adapterBinding.textViewName.text = users[position].userName
        holder.adapterBinding.textViewEmail.text = users[position].userEmail
        holder.adapterBinding.textViewAge.text = users[position].userAge.toString()
        // Take the URL information that we saved in the DB
        val imageUrl = users[position].url
        // Use picasso library to display the images
        Picasso.get().load(imageUrl).into(holder.adapterBinding.imageViewforDp, object : Callback {
            override fun onSuccess() {
                // Also show the progress bar till the image is completely loaded -> Use callback interface from Picasso library
                holder.adapterBinding.progressBarforDp.visibility = View.INVISIBLE
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, e?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })

        // Sending the user data to the UpdateUser activity and add a clickListener to each item of recyclerview
        holder.adapterBinding.layout.setOnClickListener {
            // Using the intent, we will send the data of the user to the update activity
            val intent = Intent(context, UpdateUser::class.java)
            // add the data that we send to the intent object
            intent.putExtra("id", users[position].userID)
            intent.putExtra("name", users[position].userName)
            intent.putExtra("age", users[position].userAge)
            intent.putExtra("email", users[position].userEmail)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("imageName",users[position].userImage)

            // We need to start the intent with the StartActivity(intent) but we can't start the intent. Hence, use the "context" object
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        // Returns the no. of elements in the array list as the more no.of elements in the arraylist, the more elements will be displayed in the recycler view.
        return users.size
    }

    // Get the userID based on position of the item i.e., Detect the position of the element to be deleted.
    fun getUserId(position: Int): String {
        return users[position].userID
    }

    // Get the imageName based on position of the item i.e., Detect the position of the element to be deleted.
    fun getImageName(position: Int): String {
        return users[position].userImage
    }

}