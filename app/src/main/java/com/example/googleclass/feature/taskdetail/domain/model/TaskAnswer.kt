package com.example.googleclass.feature.taskdetail.domain.model

data class TaskAnswer(
    val id: String,
    val score: Int? = null,
    val submittedAt: String? = null,
    val status: String = "NOT_COMPLETED",
    val files: List<TaskAnswerFile> = emptyList(),
    val maxScore: Int? = null,
    val postName: String? = null,
    val userId: String? = null,
    val userName: String? = null,
)

data class TaskAnswerFile(
    val id: String,
    val fileName: String? = null,
)
