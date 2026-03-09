package com.example.googleclass.feature.course.domain.model

data class Course(
    val id: String,
    val name: String,
    val description: String? = null,
    val joinCode: String? = null,
    val isArchived: Boolean = false,
    val participants: List<CourseParticipant> = emptyList(),
)
