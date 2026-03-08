package com.example.googleclass.feature.taskdetail.domain.model

enum class SubmissionStatus {
    SUBMITTED,
    OVERDUE,
    PENDING,
}

data class StudentSubmissionInfo(
    val studentId: String,
    val studentName: String,
    val score: Int?,
    val maxScore: Int,
    val status: SubmissionStatus,
)
