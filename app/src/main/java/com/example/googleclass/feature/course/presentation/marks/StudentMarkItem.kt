package com.example.googleclass.feature.course.presentation.marks

data class StudentMarkItem(
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val score: Float? = null,
    val isPassFail: Boolean = false,
)
