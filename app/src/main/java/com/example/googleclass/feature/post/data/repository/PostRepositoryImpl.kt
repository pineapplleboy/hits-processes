package com.example.googleclass.feature.post.data.repository

import com.example.googleclass.common.network.dto.IdResponseDto
import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.post.data.api.PostApi
import com.example.googleclass.feature.post.data.model.PostCreateDto
import com.example.googleclass.feature.post.data.model.PostDto
import com.example.googleclass.feature.post.data.model.PostUpdateDto
import com.example.googleclass.feature.post.domain.repository.PostRepository

class PostRepositoryImpl(
    private val postApi: PostApi,
) : PostRepository {

    override suspend fun createPost(
        courseId: String,
        post: PostCreateDto,
    ): Result<IdResponseDto> = safeApiCall(
        apiCall = { postApi.createPost(courseId, post) },
        converter = { it },
    )

    override suspend fun editPost(
        courseId: String,
        postId: String,
        post: PostUpdateDto,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { postApi.editPost(courseId, postId, post) },
    )

    override suspend fun getPost(
        courseId: String,
        postId: String,
    ): Result<PostDto> = safeApiCall(
        apiCall = { postApi.getCoursePost(courseId, postId) },
        converter = { it },
    )

    override suspend fun deletePost(
        courseId: String,
        postId: String,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { postApi.deletePost(courseId, postId) },
    )
}
