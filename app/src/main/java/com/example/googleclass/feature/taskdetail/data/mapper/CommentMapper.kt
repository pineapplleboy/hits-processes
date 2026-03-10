package com.example.googleclass.feature.taskdetail.data.mapper

import com.example.googleclass.feature.taskdetail.data.model.CommentDto
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage

fun CommentDto.toComment(): Comment = Comment(
    id = id,
    authorName = "${author.firstName.orEmpty()} ${author.lastName.orEmpty()}".trim(),
    text = text,
    createdAt = createdAt,
)

fun CommentDto.toChatMessage(studentUserId: String): ChatMessage = ChatMessage(
    id = id,
    text = text,
    authorName = "${author.firstName.orEmpty()} ${author.lastName.orEmpty()}".trim(),
    createdAt = createdAt,
    isFromTeacher = author.id != studentUserId,
)
