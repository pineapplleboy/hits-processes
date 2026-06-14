package com.example.googleclass.feature.peerreview.data.model

import com.example.googleclass.common.network.NanSafeFloatSerializer
import com.example.googleclass.common.network.dto.UserDto
import com.example.googleclass.feature.taskdetail.data.model.FileDto
import kotlinx.serialization.Serializable

@Serializable
data class PeerEvaluationDto(
    val id: String,
    val student: UserDto? = null,
    val appraiser: UserDto? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val score: Float? = null,
    val submittedAt: String? = null,
    val taskAnswerId: String? = null,
    val criteriaScores: List<ScoredMarkCriteriaDto> = emptyList(),
    val files: List<FileDto> = emptyList(),
)

@Serializable
data class ScoredMarkCriteriaDto(
    val id: String,
    @Serializable(with = NanSafeFloatSerializer::class)
    val score: Float? = null,
    val name: String? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val multiplier: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val minScore: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val maxScore: Float? = null,
    val postId: String? = null,
)
