package com.example.googleclass.feature.course.data.remote.dto

import com.example.googleclass.common.network.dto.UserModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCourseDto(
    @SerialName("userModel") val userModel: UserModel,
    @SerialName("userRole") val userRole: String,
)
