package com.example.googleclass.feature.criteria.domain.model

data class EvaluationCriterion(
    val id: String,
    val name: String,
    val description: String?,
    val evaluationFunction: EvaluationFunction,
    val multiplier: Float?,
    val minScore: Float,
    val maxScore: Float,
    val postId: String,
)

enum class EvaluationFunction {
    SUM,
    MULTIPLY,
}

val EvaluationCriterion.usesPassFailScale: Boolean
    get() = minScore == 0f && maxScore == 1f
