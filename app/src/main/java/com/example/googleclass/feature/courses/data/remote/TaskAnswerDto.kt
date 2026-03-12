package com.example.googleclass.feature.courses.data.remote

import com.example.googleclass.common.network.dto.UserModel
import com.example.googleclass.feature.taskdetail.data.model.FileModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerDto(
    val id: String,
    val score: Int? = null,
    val submittedAt: String? = null,
    val status: String = "NOT_COMPLETED",
    val files: List<FileModel> = emptyList(),
    val maxScore: Int? = null,
    val postName: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    @SerialName("user") val user: UserModel? = null,
    @SerialName("userModel") val userModel: UserModel? = null,
    @SerialName("author") val author: UserModel? = null,
)

