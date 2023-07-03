package com.turbotechnologies.firebase

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Create a service class so that the application can receive the notifications sent with FireBase. And define this class in the manifest file.
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFireBaseService : FirebaseMessagingService( ) {
    // Override the method onMessageReceived(), then our service class is ready.
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }

}