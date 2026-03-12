package com.example.googleclass.feature.courses.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CoursesApi {

    @GET("api/v1/courses/my")
    suspend fun getMyCourses(
        @Query("isArchived") isArchived: Boolean,
    ): Response<List<CourseShortDto>>

    @POST("api/v1/courses")
    suspend fun createCourse(
        @Body body: CourseCreateDto,
    ): Response<Unit>

    @PATCH("api/v1/courses/{courseId}")
    suspend fun updateCourse(
        @Path("courseId") courseId: String,
        @Body body: CourseCreateDto,
    ): Response<Unit>

    @PATCH("api/v1/courses/{courseId}/archive")
    suspend fun setCourseArchived(
        @Path("courseId") courseId: String,
        @Query("isArchived") isArchived: Boolean,
    ): Response<Unit>

    @POST("api/v1/courses/{courseId}/leave")
    suspend fun leaveCourse(
        @Path("courseId") courseId: String,
    ): Response<Unit>

    @GET("api/v1/courses/join")
    suspend fun joinCourseByCode(
        @Query("code") code: String,
    ): Response<Unit>
}
