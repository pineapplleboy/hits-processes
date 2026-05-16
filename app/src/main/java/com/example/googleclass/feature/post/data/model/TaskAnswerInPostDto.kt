package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerInPostDto(
    val id: String,
    val score: Float? = null,
    val submittedAt: String? = null,
    val status: String = "NEW",
    val files: List<AttachmentDto> = emptyList(),
    val maxScore: Float? = null,
    val postName: String? = null,
)
