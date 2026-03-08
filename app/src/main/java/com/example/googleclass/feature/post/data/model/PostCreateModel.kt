package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostCreateModel(
    val text: String,
    val files: List<String>,
    val postType: PostType,
    val maxScore: Int
)
