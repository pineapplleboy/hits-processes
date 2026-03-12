package com.example.googleclass.feature.taskdetail.domain.repository

import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage

interface CommentRepository {

    suspend fun getPostComments(postId: String): Result<List<Comment>>

    suspend fun createPostComment(postId: String, text: String): Result<Unit>

    suspend fun editPostComment(commentId: String, text: String): Result<Unit>

    suspend fun getTaskAnswerComments(taskAnswerId: String): Result<List<Comment>>

    suspend fun getTaskAnswerCommentsAsChat(
        taskAnswerId: String,
        currentUserId: String,
    ): Result<List<ChatMessage>>

    suspend fun createTaskAnswerComment(taskAnswerId: String, text: String): Result<Unit>

    suspend fun editTaskAnswerComment(commentId: String, text: String): Result<Unit>
}
