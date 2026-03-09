package com.example.googleclass.feature.course.domain.model

import java.util.Date

data class Publication(
    val id: String,
    val type: PublicationType,
    val title: String,
    val text: String? = null,
    val authorId: String,
    val createdAt: Date,
    val deadline: Date? = null,
    val files: List<String>? = null,
    val comments: List<Comment>? = null,
    val maxScore: Int? = null,
)
