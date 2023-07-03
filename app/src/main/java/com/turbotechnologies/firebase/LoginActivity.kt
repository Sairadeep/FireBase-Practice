package com.turbotechnologies.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.turbotechnologies.firebase.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var loginActivityBinding: ActivityLoginBinding
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginActivityBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginActivityBinding.root
        setContentView(view)
        loginActivityBinding.buttonSignIn.setOnClickListener {
            val userEmail = loginActivityBinding.editTextEmailLogin.text.toString()
            val userPassword = loginActivityBinding.editTextPasswordLogin.text.toString()
            // Calling the signIn method
            signInWithFireBase(userEmail, userPassword)
        }
        loginActivityBinding.buttonSignUp.setOnClickListener {
            // Clicking on the 'Sign Up' button, the Sign Up activity should open.
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        loginActivityBinding.forgotpassword.setOnClickListener {
            // A new activity will open and the user needs to enter the email address that is used while signing.
            val intent = Intent(this@LoginActivity,ForgotActivity::class.java)
            startActivity(intent)
        }
        loginActivityBinding.buttonSigninWithPhoneNumber.setOnClickListener {
            // A new activity to be opened for entering the mobile number and OTP.
            val intent = Intent(this@LoginActivity,PhoneNumberOTPActivity::class.java)
            startActivity(intent)
            finish() // To close the login Activity
        }
    }

    //Method for SignIn
    private fun signInWithFireBase(userEmail: String, userPassword: String) {
        // Necessary code will be available under the firebase documentation.
        // signInWithEmailAndPassword() function compares the "Previously created account information with the account information currently entered".
        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-In successful
                    Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Display a message as sign-in failed.
                    Toast.makeText(
                        applicationContext,
                        task.exception?.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // Remember a user and also perform the user recognition process
    override fun onStart() {
        super.onStart()
        // Create an object from the FireBase users class
        val user = auth.currentUser
        // Once the user enters the userEmail and userPassword, the FireBase auth keeps the user information until the user logs out.
        // So, we can receive this information from the firebase while the application is opening.
        if (user != null) {
            // Actions to be taken if the user logs in --> Opening the mainActivity w/o opening the login page
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            // It completes the "User Recognition Process".
        }

    }

}