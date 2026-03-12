package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttachmentModel(
    val id: String,
    val fileName: String? = null,
)
