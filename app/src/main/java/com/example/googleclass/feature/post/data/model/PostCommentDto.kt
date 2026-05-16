package com.example.googleclass.feature.post.data.model

import com.example.googleclass.common.network.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class PostCommentDto(
    val id: String,
    val text: String,
    val author: UserDto,
    val createdAt: String,
    val updatedAt: String?
)
