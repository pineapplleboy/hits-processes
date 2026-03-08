package com.example.googleclass.feature.taskdetail.domain.model

data class Submission(
    val submittedAt: String,
    val files: List<String>,
    val score: Int?,
    val maxScore: Int,
    val isNewGrade: Boolean,
)
