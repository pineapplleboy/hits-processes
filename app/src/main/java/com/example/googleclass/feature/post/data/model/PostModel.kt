package com.example.googleclass.feature.post.data.model

import com.example.googleclass.common.network.dto.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class PostModel(
    val id: String,
    val text: String,
    val author: UserModel,
    val attachments: List<AttachmentModel> = emptyList(),
    val postType: PostType,
    val createdAt: String,
    val deadline: String? = null,
    val maxScore: Int,
    val comments: List<PostCommentModel> = emptyList(),
)
