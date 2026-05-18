package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class DeleteMarkCriteriaUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        courseId: String,
        postId: String,
        markCriteriaId: String,
    ) = repository.deleteMarkCriteria(courseId, postId, markCriteriaId)
}
