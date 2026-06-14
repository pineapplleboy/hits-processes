package com.example.googleclass.feature.peerreview.data.model

import com.example.googleclass.common.network.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AppraiserTopCourseDto(
    val studentModel: UserDto? = null,
    val appraisedNumber: Int = 0,
    val matchPercentage: Int = 0,
)
