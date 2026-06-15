package com.example.googleclass.feature.peerreview.domain.model

/**
 * Одна peer-оценка: запись о том, что [appraiserName] оценил работу [studentName].
 *
 * [id] совпадает с appraiserId в эндпоинтах выставления/переопределения оценки
 * и с evaluationId в эндпоинте получения деталей.
 */
data class PeerEvaluation(
    val id: String,
    val taskAnswerId: String?,
    val studentId: String?,
    val studentName: String?,
    val appraiserId: String?,
    val appraiserName: String?,
    val score: Float?,
    val submittedAt: String?,
    val criteriaScores: List<ScoredCriterion> = emptyList(),
    val files: List<PeerReviewFile> = emptyList(),
)

data class ScoredCriterion(
    val id: String,
    val name: String?,
    val score: Float?,
    val minScore: Float?,
    val maxScore: Float?,
    val multiplier: Float?,
)

data class PeerReviewFile(
    val id: String,
    val fileName: String?,
)
