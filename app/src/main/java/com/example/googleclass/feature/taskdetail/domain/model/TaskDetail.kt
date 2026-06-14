package com.example.googleclass.feature.taskdetail.domain.model

import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType

data class TaskDetail(
    val id: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val description: String,
    val deadline: String,
    val maxScore: Int,
    val files: List<TaskFile> = emptyList(),
    val postType: String = "",
    val taskMarkEvaluationType: TaskMarkEvaluationType? = null,
    val peerReviewEnabled: Boolean = false,
)

data class TaskFile(
    val id: String,
    val fileName: String?,
)
