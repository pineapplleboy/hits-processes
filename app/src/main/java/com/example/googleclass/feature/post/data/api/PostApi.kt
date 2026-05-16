package com.example.googleclass.feature.post.data.api

import com.example.googleclass.common.network.dto.IdResponseDto
import com.example.googleclass.feature.post.data.model.PostCreateDto
import com.example.googleclass.feature.post.data.model.PostDto
import com.example.googleclass.feature.post.data.model.PostUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PostApi {

    @POST(CREATE_POST)
    suspend fun createPost(
        @Path("courseId") courseId: String,
        @Body post: PostCreateDto
    ): Response<IdResponseDto>

    @PUT(EDIT_POST)
    suspend fun editPost(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String,
        @Body post: PostUpdateDto
    ): Response<Unit>

    @DELETE(DELETE_POST)
    suspend fun deletePost(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String
    ): Response<Unit>

    @GET(GET_COURSE_POSTS)
    suspend fun getCoursePosts(@Path("courseId") courseId: String): Response<List<PostDto>>

    @GET(GET_POST)
    suspend fun getCoursePost(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String
    ): Response<PostDto>

    private companion object {
        const val CREATE_POST = "api/v1/courses/{courseId}/posts"
        const val EDIT_POST = "api/v1/courses/{courseId}/posts/{postId}"
        const val DELETE_POST = "api/v1/courses/{courseId}/posts/{postId}"
        const val GET_COURSE_POSTS = "api/v1/courses/{courseId}/posts"
        const val GET_POST = "api/v1/courses/{courseId}/posts/{postId}"
    }
}