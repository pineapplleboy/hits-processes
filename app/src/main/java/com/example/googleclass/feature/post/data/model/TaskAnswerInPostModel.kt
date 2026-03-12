package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerInPostModel(
    val id: String,
    val score: Int? = null,
    val submittedAt: String? = null,
    val status: String = "NEW",
    val files: List<AttachmentModel> = emptyList(),
    val maxScore: Int? = null,
    val postName: String? = null,
)
