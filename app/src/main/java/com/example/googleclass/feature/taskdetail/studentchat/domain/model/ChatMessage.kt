package com.example.googleclass.feature.taskdetail.studentchat.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val authorName: String,
    val createdAt: String,
    val isFromTeacher: Boolean,
)
