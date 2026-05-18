package com.example.googleclass.feature.criteria.domain.model

data class TaskAnswerCriteriaScore(
    val markCriteriaId: String,
    val name: String,
    val score: Float,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
)
