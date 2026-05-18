package com.example.googleclass.feature.criteria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarkCriteriaWriteRequestDto(
    val name: String,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float? = null,
)
