package com.example.googleclass.common.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthday: String? = null,
    val city: String? = null,
    val email: String
)
