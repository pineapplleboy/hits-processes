package com.example.googleclass.feature.course.domain.model

import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType

data class Course(
    val id: String,
    val name: String,
    val description: String? = null,
    val joinCode: String? = null,
    val isArchived: Boolean = false,
    val currentUserRole: UserRole? = null,
    val participants: List<CourseParticipant> = emptyList(),
    val courseMarkEvaluationType: TaskMarkEvaluationType? = null,
    val score: Float? = null,
)
