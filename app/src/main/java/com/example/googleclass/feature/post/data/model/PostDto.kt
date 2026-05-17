package com.example.googleclass.feature.post.data.model

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
    val maxScore: Float = 0f,
    val minScore: Float? = null,
    val multiplier: Float? = null,
    val passThreshold: Float? = null,
    val evaluationFunction: PostCreateDto.EvaluationFunction? = null,
    val comments: List<PostCommentDto> = emptyList(),
    val taskAnswer: TaskAnswerInPostDto? = null,
)
