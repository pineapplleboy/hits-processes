package com.example.googleclass.feature.criteria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CriteriaScoreRequestDto(
    val markCriteriaId: String,
    val score: Float,
)
