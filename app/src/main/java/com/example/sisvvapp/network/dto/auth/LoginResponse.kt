package com.example.sisvvapp.network.dto.auth

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val name: String
)
