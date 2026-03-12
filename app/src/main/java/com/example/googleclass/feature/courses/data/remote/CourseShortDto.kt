package com.example.googleclass.feature.courses.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseShortDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("currentUserCourseRole") val currentUserCourseRole: String? = null,
)
