package com.example.googleclass.feature.peerreview.domain.model

/**
 * Строка рейтинга оценщиков курса.
 *
 * [appraisedNumber] — сколько работ оценил студент,
 * [matchPercentage] — процент совпадения его оценок с итоговыми.
 */
data class AppraiserTopEntry(
    val studentId: String?,
    val studentName: String?,
    val appraisedNumber: Int,
    val matchPercentage: Int,
)
