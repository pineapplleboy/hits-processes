package com.example.googleclass.feature.course.data.mapper

import com.example.googleclass.common.network.dto.UserModel
import com.example.googleclass.feature.course.data.remote.dto.CourseDto
import com.example.googleclass.feature.course.data.remote.dto.UserCourseDto
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.CourseParticipant
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole

fun CourseDto.toDomain(participants: List<CourseParticipant> = emptyList()): Course = Course(
    id = id,
    name = name,
    description = description,
    joinCode = joinCode,
    isArchived = isArchived,
    participants = participants,
)

fun UserModel.toDomain(): User = User(
    id = id,
    name = listOfNotNull(firstName, lastName)
        .joinToString(" ")
        .ifBlank { email },
    email = email,
)

fun UserCourseDto.toParticipant(): CourseParticipant = CourseParticipant(
    userId = userModel.id,
    role = userRole.toUserRole(),
)

fun UserCourseDto.toUser(): User = userModel.toDomain()

private fun String.toUserRole(): UserRole = when (uppercase()) {
    "HEAD_TEACHER" -> UserRole.MAIN_TEACHER
    "TEACHER" -> UserRole.TEACHER
    "STUDENT" -> UserRole.STUDENT
    else -> UserRole.STUDENT
}
