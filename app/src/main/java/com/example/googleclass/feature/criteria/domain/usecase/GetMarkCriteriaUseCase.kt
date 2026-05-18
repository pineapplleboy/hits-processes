package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class GetMarkCriteriaUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        courseId: String,
        postId: String,
    ) = repository.getMarkCriteria(courseId, postId)
}
