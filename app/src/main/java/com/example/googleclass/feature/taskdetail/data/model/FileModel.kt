package com.example.googleclass.feature.taskdetail.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FileModel(
    val id: String,
    val fileName: String? = null,
)
