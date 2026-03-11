package com.example.googleclass.feature.course.data.remote

import com.example.googleclass.feature.course.data.remote.dto.CourseDto
import com.example.googleclass.feature.course.data.remote.dto.UserCourseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseDetailApi {

    @GET("api/v1/courses/{courseId}")
    suspend fun getCourse(@Path("courseId") courseId: String): Response<CourseDto>

    @GET("api/v1/courses/{courseId}/users")
    suspend fun getCourseUsers(@Path("courseId") courseId: String): Response<List<UserCourseDto>>

    @POST("api/v1/courses/{courseId}/users/{userID}/role")
    suspend fun changeUserRole(
        @Path("courseId") courseId: String,
        @Path("userID") userId: String,
        @Query("newUserRole") newUserRole: String,
    ): Response<Unit>

    @POST("api/v1/courses/{courseId}/users/{userID}/remove")
    suspend fun removeUserFromCourse(
        @Path("courseId") courseId: String,
        @Path("userID") userId: String,
    ): Response<Unit>
}
