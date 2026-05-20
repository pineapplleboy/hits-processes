package com.example.googleclass.feature.criteria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerCriteriaScoreDto(
    val markCriteriaId: String,
    val name: String,
    val score: Float? = null,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float? = null,
)
