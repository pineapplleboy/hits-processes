package com.example.googleclass.feature.authorization.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    @SerialName("statusCode") val statusCode: Int,
    @SerialName("errors") val errors: List<String>? = null,
)
