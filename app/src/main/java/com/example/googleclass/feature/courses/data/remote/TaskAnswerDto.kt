package com.example.googleclass.feature.courses.data.remote

import com.example.googleclass.common.network.dto.UserDto
import com.example.googleclass.feature.taskdetail.data.model.FileDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerDto(
    val id: String,
    val score: Float? = null,
    val submittedAt: String? = null,
    val status: String = "NOT_COMPLETED",
    val files: List<FileDto> = emptyList(),
    val maxScore: Float? = null,
    val postName: String? = null,
    val postId: String? = null,
    val courseId: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("userModel") val userModel: UserDto? = null,
    @SerialName("author") val author: UserDto? = null
)

