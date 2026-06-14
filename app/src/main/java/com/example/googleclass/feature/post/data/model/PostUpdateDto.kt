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
    val appraiserDeadline: String? = null,
    val studentAppraisingNumber: Int? = null,
    val taskAnswerAppraisingType: TaskAnswerAppraisingType? = null,
    val canSeeAppraiser: Boolean? = null,
    val canSeeAppraised: Boolean? = null,
)
