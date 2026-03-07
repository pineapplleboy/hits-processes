package com.example.googleclass.feature.authorization.domain.model

data class RegisterData(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val birthday: String,
    val city: String,
)
