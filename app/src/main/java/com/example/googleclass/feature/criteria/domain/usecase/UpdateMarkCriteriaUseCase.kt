package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class UpdateMarkCriteriaUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        courseId: String,
        postId: String,
        markCriteriaId: String,
        draft: MarkCriteriaDraft,
    ) = repository.updateMarkCriteria(courseId, postId, markCriteriaId, draft)
}
