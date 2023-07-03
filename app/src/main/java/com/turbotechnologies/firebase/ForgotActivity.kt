package com.turbotechnologies.firebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.turbotechnologies.firebase.databinding.ActivityForgotBinding

class ForgotActivity : AppCompatActivity() {
    lateinit var forgotBinding: ActivityForgotBinding
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forgotBinding = ActivityForgotBinding.inflate(layoutInflater)
        val view = forgotBinding.root
        setContentView(view)
        forgotBinding.buttonResetPassword.setOnClickListener {
            // A reset link needs to sent to the email and to do so first we need to create an object from the fireBase auth class.
            val email = forgotBinding.editTextEmailReset.text.toString()
            // Reset process is done based on this email address
            auth.sendPasswordResetEmail(
                // auth.sendPasswordResetEmail() -> This method will automatically send the reset link to the user's email.
                email
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Password reset link has been sent to your email",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        task.exception?.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}