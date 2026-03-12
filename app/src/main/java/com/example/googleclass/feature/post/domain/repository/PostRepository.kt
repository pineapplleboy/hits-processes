package com.example.googleclass.feature.post.domain.repository

import com.example.googleclass.common.network.dto.IdResponseModel
import com.example.googleclass.feature.post.data.model.PostCreateModel
import com.example.googleclass.feature.post.data.model.PostModel
import com.example.googleclass.feature.post.data.model.PostUpdateModel

interface PostRepository {

    suspend fun createPost(
        courseId: String,
        post: PostCreateModel,
    ): Result<IdResponseModel>

    suspend fun editPost(
        courseId: String,
        postId: String,
        post: PostUpdateModel,
    ): Result<Unit>

    suspend fun getPost(
        courseId: String,
        postId: String,
    ): Result<PostModel>

    suspend fun deletePost(
        courseId: String,
        postId: String,
    ): Result<Unit>
}
