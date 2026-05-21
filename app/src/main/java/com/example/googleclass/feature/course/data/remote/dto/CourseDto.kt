package com.example.googleclass.feature.course.data.remote.dto

import com.example.googleclass.common.network.NanSafeFloatSerializer
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
    @SerialName("courseMarkEvaluationType") val courseMarkEvaluationType: String? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    @SerialName("passThreshold") val passThreshold: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    @SerialName("score") val score: Float? = null
)
