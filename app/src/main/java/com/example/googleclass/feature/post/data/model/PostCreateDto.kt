package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostCreateDto(
    val text: String,
    val files: List<AttachmentDto>,
    val postType: PostType,
    val taskMarkEvaluationType: TaskMarkEvaluationType? = null,
    val maxScore: Float? = null,
    val minScore: Float? = null,
    val multiplier: Float? = null,
    val passThreshold: Float? = null,
    val evaluationFunction: EvaluationFunction? = null,
    val deadline: String? = null,
    val appraiserDeadline: String? = null,
    val studentAppraisingNumber: Int? = null,
    val taskAnswerAppraisingType: TaskAnswerAppraisingType? = null,
    val canSeeAppraiser: Boolean? = null,
    val canSeeAppraised: Boolean? = null,
) {
    @Serializable
    enum class EvaluationFunction {
        SUM,
        MULTIPLY,
    }
}
