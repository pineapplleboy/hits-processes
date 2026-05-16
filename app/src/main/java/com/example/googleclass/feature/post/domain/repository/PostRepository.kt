package com.example.googleclass.feature.post.domain.repository

import com.example.googleclass.common.network.dto.IdResponseDto
import com.example.googleclass.feature.post.data.model.PostCreateDto
import com.example.googleclass.feature.post.data.model.PostDto
import com.example.googleclass.feature.post.data.model.PostUpdateDto

interface PostRepository {

    suspend fun createPost(
        courseId: String,
        post: PostCreateDto,
    ): Result<IdResponseDto>

    suspend fun editPost(
        courseId: String,
        postId: String,
        post: PostUpdateDto,
    ): Result<Unit>

    suspend fun getPost(
        courseId: String,
        postId: String,
    ): Result<PostDto>

    suspend fun deletePost(
        courseId: String,
        postId: String,
    ): Result<Unit>
}
