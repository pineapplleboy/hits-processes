package com.example.googleclass.feature.course.domain.model

data class Course(
    val id: String,
    val name: String,
    val participants: List<CourseParticipant>,
)
