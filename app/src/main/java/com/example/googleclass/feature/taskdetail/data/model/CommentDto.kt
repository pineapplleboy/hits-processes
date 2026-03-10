package com.example.googleclass.feature.taskdetail.data.model

import com.example.googleclass.common.network.dto.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    val text: String,
    val author: UserModel,
    val createdAt: String,
    val updatedAt: String? = null,
)

@Serializable
data class CommentCreateDto(
    val text: String,
)

@Serializable
data class CommentEditDto(
    val text: String,
)
