package com.example.googleclass.feature.courses.data.remote

import com.example.googleclass.feature.taskdetail.data.model.FileModel
import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerDto(
    val id: String,
    val score: Int? = null,
    val submittedAt: String? = null,
    val status: String = "NOT_COMPLETED",
    val files: List<FileModel> = emptyList(),
    val maxScore: Int? = null,
    val postName: String? = null,
)

