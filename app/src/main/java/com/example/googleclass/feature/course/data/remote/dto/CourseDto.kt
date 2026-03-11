package com.example.googleclass.feature.course.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("joinCode") val joinCode: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("isArchived") val isArchived: Boolean = false,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("currentUserCourseRole") val currentUserCourseRole: String? = null,
)
