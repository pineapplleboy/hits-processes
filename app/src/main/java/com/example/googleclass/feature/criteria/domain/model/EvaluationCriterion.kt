package com.example.googleclass.feature.criteria.domain.model

data class EvaluationCriterion(
    val id: String,
    val title: String,
    val grading: CriterionGrading,
)

sealed interface CriterionGrading {
    data object PassFail : CriterionGrading

    data class Range(
        val minValue: Int,
        val maxValue: Int,
        val multiplier: Float? = null,
        val maxPoints: Int? = null,
    ) : CriterionGrading
}
