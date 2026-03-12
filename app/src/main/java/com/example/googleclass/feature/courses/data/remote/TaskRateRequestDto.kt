package com.example.googleclass.feature.courses.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TaskRateRequestDto(
    val rate: Int,
)
