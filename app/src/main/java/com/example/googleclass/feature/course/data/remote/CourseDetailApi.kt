package com.example.googleclass.feature.course.data.remote

import com.example.googleclass.feature.course.data.remote.dto.CourseDto
import com.example.googleclass.feature.course.data.remote.dto.UserCourseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseDetailApi {

    @GET("api/v1/courses/{courseId}")
    suspend fun getCourse(@Path("courseId") courseId: String): Response<CourseDto>

    @GET("api/v1/courses/{courseId}/users")
    suspend fun getCourseUsers(@Path("courseId") courseId: String): Response<List<UserCourseDto>>
}
