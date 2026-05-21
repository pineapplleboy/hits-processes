package com.example.googleclass.feature.criteria.domain.model

data class MarkCriteriaDraft(
    val name: String,
    val description: String?,
    val evaluationFunction: EvaluationFunction,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
)
