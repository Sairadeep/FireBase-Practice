package com.turbotechnologies.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.turbotechnologies.firebase.databinding.ActivityPhoneNumberOtpactivityBinding
import java.util.concurrent.TimeUnit

class PhoneNumberOTPActivity : AppCompatActivity() {
    private lateinit var phoneNumberOtpactivityBinding: ActivityPhoneNumberOtpactivityBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Creating a call back object
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var verificationCode = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumberOtpactivityBinding =
            ActivityPhoneNumberOtpactivityBinding.inflate(layoutInflater)
        val view = phoneNumberOtpactivityBinding.root
        setContentView(view)

        phoneNumberOtpactivityBinding.buttonSendVerifyCode.setOnClickListener {
            val phoneNumber = phoneNumberOtpactivityBinding.editTextPhone.text.toString()
            // For the phone number authentication firebase, we can use the "Phone Auth options class". And it takes the object from the "FireBaseAuth" class as a parameter.
            // Also set the time out i.e., the validity period of the incoming message.
            val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber).setTimeout(
                // Parameter 1-> Validity of the code 60L -> 60 Seconds and L -> Long time
                60L,
                TimeUnit.SECONDS
            ).setActivity(
                // In which activity the incoming code will be verified
                this@PhoneNumberOTPActivity
            )
                .setCallbacks(mCallbacks) // 'mcallBacks' is the object that we create from the "Callback" class
                .build()

            // Use the "VerifyPhoneNumber" method from the "PhoneAuthProvider" class
            // It initiates the phone number verification process i.e., sends a verification code to the user provided phone number.
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        phoneNumberOtpactivityBinding.buttontoVerifyCode.setOnClickListener {
            signInWithSmsCode()
        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                TODO("Not yet implemented")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                TODO("Not yet implemented")
            }

            // Add code sent method as well
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                // FireBase sends a single use code with p0 parameter and it is assigned to p0
                // Assign the incoming code to the verification code string. Then this code can be used anywhere
                verificationCode = p0
                super.onCodeSent(p0, p1)
            }
        }
    }

    private fun signInWithSmsCode() {
        // Getting the code entered by the user.
        val userCode = phoneNumberOtpactivityBinding.editTextverificationCode.text.toString()
        // Creating an object from the phone auth credentials class which represents the user's phone number authentication credentials.
        // It is typically used after the user has successfully entered the verification code received via SMS.
        val credentials = PhoneAuthProvider.getCredential(
            // Parameter 1 -> Code sent from firebase i.e., Verification code
            // Parameter 2 -> code entered by user
            verificationCode,
            userCode
            // FireBase will automatically check these two as part of login authentication.
        )
        signInWithPhoneAuthCredentials(credentials)
    }

    private fun signInWithPhoneAuthCredentials(credentials: PhoneAuthCredential) {
        // Login Process
        auth.signInWithCredential(credentials).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_LONG).show()
                val intent = Intent(this@PhoneNumberOTPActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, task.exception?.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}