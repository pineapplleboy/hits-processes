package com.example.googleclass.feature.courses.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoursesApi {

    @GET("api/v1/courses/my")
    suspend fun getMyCourses(
        @Query("isArchived") isArchived: Boolean,
    ): Response<List<CourseShortDto>>
}
