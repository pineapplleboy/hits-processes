package com.example.googleclass.feature.taskdetail.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FileDto(
    val id: String,
    val fileName: String? = null,
)
