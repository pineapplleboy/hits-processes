package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttachmentDto(
    val id: String,
    val fileName: String? = null,
)
