package com.example.googleclass.feature.peerreview.data.model

import com.example.googleclass.common.network.dto.UserDto
import com.example.googleclass.feature.taskdetail.data.model.FileDto
import kotlinx.serialization.Serializable

@Serializable
data class AvailablePeerEvaluationDto(
    val taskAnswerId: String,
    val student: UserDto? = null,
    val submittedAt: String? = null,
    val canAppraise: Boolean = false,
    val unavailableReason: String? = null,
    val files: List<FileDto> = emptyList(),
)
