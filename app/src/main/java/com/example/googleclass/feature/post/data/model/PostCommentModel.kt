package com.example.googleclass.feature.post.data.model

import com.example.googleclass.common.network.dto.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class PostCommentModel(
    val id: String,
    val text: String,
    val author: UserModel,
    val createdAt: String,
    val updatedAt: String?
)
