package com.example.googleclass.feature.post.data.model

import com.example.googleclass.common.network.NanSafeFloatSerializer
import com.example.googleclass.common.network.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: String,
    val text: String,
    val author: UserDto,
    val files: List<AttachmentDto> = emptyList(),
    val postType: PostType,
    val taskMarkEvaluationType: TaskMarkEvaluationType? = null,
    val createdAt: String,
    val deadline: String? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val maxScore: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val minScore: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val multiplier: Float? = null,
    @Serializable(with = NanSafeFloatSerializer::class)
    val passThreshold: Float? = null,
    val evaluationFunction: PostCreateDto.EvaluationFunction? = null,
    val comments: List<PostCommentDto> = emptyList(),
    val taskAnswer: TaskAnswerInPostDto? = null,
    val appraiserDeadline: String? = null,
    val studentAppraisingNumber: Int? = null,
    val taskAnswerAppraisingType: TaskAnswerAppraisingType? = null,
    val canSeeAppraiser: Boolean? = null,
    val canSeeAppraised: Boolean? = null,
)
