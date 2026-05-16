package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostUpdateDto(
    val text: String,
    val files: List<AttachmentDto>
)
