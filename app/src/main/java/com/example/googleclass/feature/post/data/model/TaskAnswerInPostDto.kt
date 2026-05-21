package com.example.googleclass.feature.post.data.model

import com.example.googleclass.common.network.NanSafeFloatSerializer
import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerInPostDto(
    val id: String,
    @Serializable(with = NanSafeFloatSerializer::class)
    val score: Float? = null,
    val submittedAt: String? = null,
    val status: String = "NEW",
    val evaluationStatus: String = "NOT_EVALUATED",
    val files: List<AttachmentDto> = emptyList(),
    @Serializable(with = NanSafeFloatSerializer::class)
    val maxScore: Float? = null,
    val postName: String? = null,
)
