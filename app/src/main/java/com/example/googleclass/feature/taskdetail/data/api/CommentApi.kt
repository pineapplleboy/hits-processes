package com.example.googleclass.feature.taskdetail.data.api

import com.example.googleclass.feature.taskdetail.data.model.CommentCreateDto
import com.example.googleclass.feature.taskdetail.data.model.CommentDto
import com.example.googleclass.feature.taskdetail.data.model.CommentEditDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentApi {

    @GET(GET_POST_COMMENTS)
    suspend fun getPostComments(
        @Path("postId") postId: String,
    ): Response<List<CommentDto>>

    @POST(CREATE_POST_COMMENT)
    suspend fun createPostComment(
        @Path("postId") postId: String,
        @Body body: CommentCreateDto,
    ): Response<Unit>

    @PATCH(EDIT_POST_COMMENT)
    suspend fun editPostComment(
        @Path("postCommentId") commentId: String,
        @Body body: CommentEditDto,
    ): Response<Unit>

    @GET(GET_TASK_ANSWER_COMMENTS)
    suspend fun getTaskAnswerComments(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<List<CommentDto>>

    @POST(CREATE_TASK_ANSWER_COMMENT)
    suspend fun createTaskAnswerComment(
        @Path("taskAnswerId") taskAnswerId: String,
        @Body body: CommentCreateDto,
    ): Response<Unit>

    @PATCH(EDIT_TASK_ANSWER_COMMENT)
    suspend fun editTaskAnswerComment(
        @Path("taskAnswerCommentId") commentId: String,
        @Body body: CommentEditDto,
    ): Response<Unit>

    private companion object {
        const val GET_POST_COMMENTS = "api/v1/post/{postId}/comments"
        const val CREATE_POST_COMMENT = "api/v1/post/{postId}/comments"
        const val EDIT_POST_COMMENT = "api/v1/post/comments/{postCommentId}"
        const val GET_TASK_ANSWER_COMMENTS = "api/v1/task-answer/{taskAnswerId}/comments"
        const val CREATE_TASK_ANSWER_COMMENT = "api/v1/task-answer/{taskAnswerId}/comments"
        const val EDIT_TASK_ANSWER_COMMENT = "api/v1/task-answer/comments/{taskAnswerCommentId}"
    }
}
