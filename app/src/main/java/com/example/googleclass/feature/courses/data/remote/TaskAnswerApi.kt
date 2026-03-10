package com.example.googleclass.feature.courses.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface TaskAnswerApi {

    @GET("api/v1/task-answer/all")
    suspend fun getAllUserTaskAnswers(): Response<List<TaskAnswerDto>>
}

