package com.example.banksampah.data

data class User(
    var uid: String = "",
    var fullName: String = "",
    var displayName: String = "",
    var email: String = "",
    var profilePhotoUrl: String = "",
    var isAdmin: Boolean = false,
    var createdAt: Long = 0,
    // Data dari UserProfile (tetap ada!)
    var address: String = "",
    var phoneNumber: String = "",
    var nik: String = ""
)