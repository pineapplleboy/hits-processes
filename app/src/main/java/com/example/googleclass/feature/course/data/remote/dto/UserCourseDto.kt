package com.example.googleclass.feature.course.data.remote.dto

import com.example.googleclass.common.network.dto.UserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCourseDto(
    @SerialName("userModel") val userModel: UserDto,
    @SerialName("userRole") val userRole: String,
    @SerialName("score") val score: Float? = null
)
