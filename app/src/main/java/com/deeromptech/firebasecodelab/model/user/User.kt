package com.deeromptech.firebasecodelab.model.user

data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    var imagePath: String = "",
    val role: String? = ""
)
