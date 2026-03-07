package com.example.googleclass.feature.course.domain.model

import java.util.Date

data class Submission(
    val id: String,
    val assignmentId: String,
    val userId: String,
    val submittedAt: Date? = null,
    val grade: Int? = null,
    val maxScore: Int? = null,
)
