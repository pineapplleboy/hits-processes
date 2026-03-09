package com.example.googleclass.feature.course.domain.repository

import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.User

interface CourseDetailRepository {

    suspend fun getCourse(courseId: String): Result<Course>

    suspend fun getCoursePosts(courseId: String): Result<List<Publication>>

    suspend fun getCourseWithParticipantsAndUsers(courseId: String): Result<CourseDetailResult>
}

data class CourseDetailResult(
    val course: Course,
    val publications: List<Publication>,
    val users: Map<String, User>,
)
