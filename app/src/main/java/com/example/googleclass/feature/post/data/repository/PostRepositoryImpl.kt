package com.example.googleclass.feature.post.data.repository

import com.example.googleclass.common.network.dto.IdResponseModel
import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.post.data.api.PostApi
import com.example.googleclass.feature.post.data.model.PostCreateModel
import com.example.googleclass.feature.post.data.model.PostModel
import com.example.googleclass.feature.post.data.model.PostUpdateModel
import com.example.googleclass.feature.post.domain.repository.PostRepository

class PostRepositoryImpl(
    private val postApi: PostApi,
) : PostRepository {

    override suspend fun createPost(
        courseId: String,
        post: PostCreateModel,
    ): Result<IdResponseModel> = safeApiCall(
        apiCall = { postApi.createPost(courseId, post) },
        converter = { it },
    )

    override suspend fun editPost(
        courseId: String,
        postId: String,
        post: PostUpdateModel,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { postApi.editPost(courseId, postId, post) },
    )

    override suspend fun getPost(
        courseId: String,
        postId: String,
    ): Result<PostModel> = safeApiCall(
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
