package com.example.googleclass.feature.taskdetail.domain.model

data class TaskDetail(
    val id: String,
    val title: String,
    val authorName: String,
    val createdAt: String,
    val description: String,
    val deadline: String,
    val maxScore: Int,
)
