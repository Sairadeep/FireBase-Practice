package com.turbotechnologies.firebase

data class Users(
    val userID: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userAge: Int = 0,
    val url: String = "",
    val userImage: String = ""
) {
    // It is a data clas or a modal class and define the variables corresponding to the data that we receive from the users.
    // ID is not taken from the user and it is automatically generated a unique key value using FireBase unique key generation feature.

}