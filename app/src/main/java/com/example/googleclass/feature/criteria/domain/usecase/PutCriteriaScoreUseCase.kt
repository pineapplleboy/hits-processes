package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class PutCriteriaScoreUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        taskAnswerId: String,
        draft: CriteriaScoreDraft,
    ) = repository.putCriteriaScore(taskAnswerId, draft)
}
