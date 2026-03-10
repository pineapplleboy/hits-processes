package com.example.googleclass.feature.courses.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerDto(
    val id: String,
    val score: Int? = null,
    val submittedAt: String? = null,
    val status: String = "NOT_COMPLETED",
)

