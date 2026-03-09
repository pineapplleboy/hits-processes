package com.example.googleclass.feature.course.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.feature.course.data.mapper.toDomain
import com.example.googleclass.feature.course.data.mapper.toParticipant
import com.example.googleclass.feature.course.data.mapper.toPublication
import com.example.googleclass.feature.course.data.mapper.toUser
import com.example.googleclass.feature.course.data.remote.CourseDetailApi
import com.example.googleclass.feature.course.domain.repository.CourseDetailRepository
import com.example.googleclass.feature.course.domain.repository.CourseDetailResult
import com.example.googleclass.feature.post.data.api.PostApi
import com.example.googleclass.feature.post.data.model.PostModel

class CourseDetailRepositoryImpl(
    private val courseDetailApi: CourseDetailApi,
    private val postApi: PostApi,
) : CourseDetailRepository {

    override suspend fun getCourse(courseId: String): Result<com.example.googleclass.feature.course.domain.model.Course> = safeApiCall(
        apiCall = { courseDetailApi.getCourse(courseId) },
        converter = { it.toDomain() },
    )

    override suspend fun getCoursePosts(courseId: String): Result<List<com.example.googleclass.feature.course.domain.model.Publication>> = safeApiCall(
        apiCall = { postApi.getCoursePosts(courseId) },
        converter = { list: List<PostModel> -> list.map { it.toPublication() } },
    )

    override suspend fun getCourseWithParticipantsAndUsers(courseId: String): Result<CourseDetailResult> {
        val courseResult = safeApiCall(
            apiCall = { courseDetailApi.getCourse(courseId) },
            converter = { it.toDomain() },
        )
        if (courseResult.isFailure) return Result.failure(courseResult.exceptionOrNull()!!)

        val usersResult = safeApiCall(
            apiCall = { courseDetailApi.getCourseUsers(courseId) },
            converter = { list ->
                val participants = list.map { it.toParticipant() }
                val usersMap = list.associate { it.userModel.id to it.toUser() }
                Pair(participants, usersMap)
            },
        )
        if (usersResult.isFailure) return Result.failure(usersResult.exceptionOrNull()!!)

        val (participants, usersFromParticipants) = usersResult.getOrThrow()
        val course = courseResult.getOrThrow().copy(participants = participants)

        val postsResult = safeApiCall(
            apiCall = { postApi.getCoursePosts(courseId) },
            converter = { it },
        )
        if (postsResult.isFailure) return Result.failure(postsResult.exceptionOrNull()!!)

        val posts = postsResult.getOrThrow()
        val publications = posts.map { it.toPublication() }
        val usersFromPosts = posts.associate { it.author.id to it.author.toDomain() }
        val allUsers = usersFromParticipants + usersFromPosts

        return Result.success(
            CourseDetailResult(
                course = course,
                publications = publications,
                users = allUsers,
            )
        )
    }
}
