package com.example.googleclass.feature.course.domain.model

import java.util.Date

data class Comment(
    val userId: String,
    val text: String,
    val createdAt: Date,
)
