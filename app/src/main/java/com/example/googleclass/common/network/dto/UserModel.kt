package com.example.googleclass.common.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthday: String,
    val city: String,
    val email: String
)
