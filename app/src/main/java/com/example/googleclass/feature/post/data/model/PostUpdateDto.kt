package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostUpdateDto(
    val text: String,
    val files: List<AttachmentDto>,
    val taskMarkEvaluationType: TaskMarkEvaluationType? = null,
    val maxScore: Float? = null,
    val minScore: Float? = null,
    val multiplier: Float? = null,
    val passThreshold: Float? = null,
    val evaluationFunction: PostCreateDto.EvaluationFunction? = null,
)
