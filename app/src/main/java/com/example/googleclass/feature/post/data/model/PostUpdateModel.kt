package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostUpdateModel(
    val text: String,
    val files: List<AttachmentModel>
)
