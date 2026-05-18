package com.example.googleclass.feature.criteria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarkCriteriaDto(
    val id: String,
    val evaluationFunction: EvaluationFunctionDto,
    val name: String,
    val multiplier: Float? = null,
    val minScore: Float,
    val maxScore: Float,
    val postId: String,
)

@Serializable
enum class EvaluationFunctionDto {
    SUM,
    MULTIPLY,
}
