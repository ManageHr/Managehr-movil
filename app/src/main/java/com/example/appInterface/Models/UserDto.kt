package com.example.appinterface.Models

data class UserDto (
    val id: Long? = null,
    val name: String,
    val email: String,
    val password: String,
    val rol: String
)