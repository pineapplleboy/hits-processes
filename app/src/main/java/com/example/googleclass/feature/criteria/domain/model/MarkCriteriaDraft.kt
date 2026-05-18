package com.example.googleclass.feature.criteria.domain.model

data class MarkCriteriaDraft(
    val name: String,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
)
