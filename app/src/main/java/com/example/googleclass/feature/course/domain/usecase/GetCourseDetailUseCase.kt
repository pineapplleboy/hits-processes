package com.example.googleclass.feature.course.domain.usecase

import com.example.googleclass.feature.course.domain.repository.CourseDetailRepository
import com.example.googleclass.feature.course.domain.repository.CourseDetailResult

class GetCourseDetailUseCase(
    private val repository: CourseDetailRepository,
) {

    suspend operator fun invoke(courseId: String): Result<CourseDetailResult> =
        repository.getCourseWithParticipantsAndUsers(courseId)
}
