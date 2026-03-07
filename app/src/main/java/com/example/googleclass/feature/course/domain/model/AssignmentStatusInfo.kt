package com.example.googleclass.feature.course.domain.model

data class AssignmentStatusInfo(
    val status: AssignmentStatus,
    val text: String,
    val grade: Int? = null,
    val maxScore: Int? = null,
)
