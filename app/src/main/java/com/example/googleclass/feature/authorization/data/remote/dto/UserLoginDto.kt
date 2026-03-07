package com.example.googleclass.feature.authorization.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)
