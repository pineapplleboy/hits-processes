package com.example.googleclass.feature.criteria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarkCriteriaWriteRequestDto(
    val name: String,
    val description: String? = null,
    val evaluationFunction: EvaluationFunctionDto = EvaluationFunctionDto.SUM,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float? = null,
)
