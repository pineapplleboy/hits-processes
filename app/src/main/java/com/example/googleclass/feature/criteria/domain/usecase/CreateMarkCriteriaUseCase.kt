package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class CreateMarkCriteriaUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        courseId: String,
        postId: String,
        draft: MarkCriteriaDraft,
    ) = repository.createMarkCriteria(courseId, postId, draft)
}
