package com.example.googleclass.feature.courses.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CourseCreateDto(
    val name: String,
    val description: String,
)

