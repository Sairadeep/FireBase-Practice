package com.turbotechnologies.firebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.turbotechnologies.firebase.databinding.ActivitySignUpBinding
import kotlin.math.sign

class SignUpActivity : AppCompatActivity() {
    lateinit var signUpBinding: ActivitySignUpBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = signUpBinding.root
        setContentView(view)
        signUpBinding.buttonSignUp.setOnClickListener {
            // Define the "Login Activity" as the parent activity of the "Sign Up" activity
            // Getting the email & password entered by the user in the sign up page
            val userEmail = signUpBinding.editTextEmailSignUp.text.toString()
            val userPassword = signUpBinding.editTextPasswordSignUp.text.toString()
            signUpWithFirebase(userEmail, userPassword)
        }
    }

    // Membership function
    private fun signUpWithFirebase(userEmail: String, userPassword: String) {
        // Create an object from the FireBase Auth class in the global area as that object will be used for "Sign Up".
        auth.createUserWithEmailAndPassword(
            // This Function takes "Email" and "Password" entered by the user as a parameter to create an account.
            userEmail,userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"Account has been created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(applicationContext,task.exception?.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}