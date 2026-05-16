package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostCreateDto(
    val text: String,
    val files: List<AttachmentDto>,
    val postType: PostType,
    val maxScore: Int,
    val deadline: String
)
