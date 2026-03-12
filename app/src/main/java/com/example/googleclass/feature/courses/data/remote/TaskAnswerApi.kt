package com.example.googleclass.feature.courses.data.remote

import com.example.googleclass.feature.taskdetail.data.model.FileModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskAnswerApi {

    @GET("api/v1/task-answer/all")
    suspend fun getAllUserTaskAnswers(): Response<List<TaskAnswerDto>>

    @GET("api/v1/task-answer/post/{postId}")
    suspend fun getUserPostTaskAnswer(
        @Path("postId") postId: String,
    ): Response<TaskAnswerDto>

    @GET("api/v1/task-answer/post/{postId}/all")
    suspend fun getAllPostTaskAnswers(
        @Path("postId") postId: String,
    ): Response<List<TaskAnswerDto>>

    @PUT("api/v1/task-answer/task-answer/{taskAnswerId}/evaluate")
    suspend fun evaluateTask(
        @Path("taskAnswerId") taskAnswerId: String,
        @Body body: TaskRateRequestDto,
    ): Response<Unit>

    @POST("api/v1/task-answer/submit/{taskAnswerId}")
    suspend fun submitTask(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<Unit>

    @DELETE("api/v1/task-answer/submit/{taskAnswerId}")
    suspend fun unsubmitTask(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<Unit>

    @POST("api/v1/task-answer/pin-file/{taskAnswerId}")
    suspend fun appendFiles(
        @Path("taskAnswerId") taskAnswerId: String,
        @Body files: List<FileModel>,
    ): Response<Unit>

    @DELETE("api/v1/task-answer/unpin-file/{taskAnswerId}/file/{fileId}")
    suspend fun unpinFile(
        @Path("taskAnswerId") taskAnswerId: String,
        @Path("fileId") fileId: String,
    ): Response<Unit>
}

