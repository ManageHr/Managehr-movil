package com.example.appinterface.Models

// En Models/UserDto.kt
data class UserDto(
    val id: Long? = null,
    val name: String,
    val email: String,
    val password: String,
    val rol: Int = 2,
    val remember_token: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)