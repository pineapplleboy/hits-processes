package com.example.googleclass.feature.taskdetail.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.taskdetail.data.api.CommentApi
import com.example.googleclass.feature.taskdetail.data.mapper.toChatMessage
import com.example.googleclass.feature.taskdetail.data.mapper.toComment
import com.example.googleclass.feature.taskdetail.data.model.CommentCreateDto
import com.example.googleclass.feature.taskdetail.data.model.CommentEditDto
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage

class CommentRepositoryImpl(
    private val commentApi: CommentApi,
) : CommentRepository {

    override suspend fun getPostComments(postId: String): Result<List<Comment>> = safeApiCall(
        apiCall = { commentApi.getPostComments(postId) },
        converter = { dtos -> dtos.map { it.toComment() } },
    )

    override suspend fun createPostComment(postId: String, text: String): Result<Unit> =
        safeApiCallUnit {
            commentApi.createPostComment(postId, CommentCreateDto(text))
        }

    override suspend fun editPostComment(commentId: String, text: String): Result<Unit> =
        safeApiCallUnit {
            commentApi.editPostComment(commentId, CommentEditDto(text))
        }

    override suspend fun getTaskAnswerComments(taskAnswerId: String): Result<List<Comment>> =
        safeApiCall(
            apiCall = { commentApi.getTaskAnswerComments(taskAnswerId) },
            converter = { dtos -> dtos.map { it.toComment() } },
        )

    override suspend fun getTaskAnswerCommentsAsChat(
        taskAnswerId: String,
        studentUserId: String,
    ): Result<List<ChatMessage>> = safeApiCall(
        apiCall = { commentApi.getTaskAnswerComments(taskAnswerId) },
        converter = { dtos -> dtos.map { it.toChatMessage(studentUserId) } },
    )

    override suspend fun createTaskAnswerComment(
        taskAnswerId: String,
        text: String,
    ): Result<Unit> = safeApiCallUnit {
        commentApi.createTaskAnswerComment(taskAnswerId, CommentCreateDto(text))
    }

    override suspend fun editTaskAnswerComment(
        commentId: String,
        text: String,
    ): Result<Unit> = safeApiCallUnit {
        commentApi.editTaskAnswerComment(commentId, CommentEditDto(text))
    }
}
