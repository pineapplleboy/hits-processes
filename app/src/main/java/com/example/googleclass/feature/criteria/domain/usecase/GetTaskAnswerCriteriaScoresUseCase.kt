package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class GetTaskAnswerCriteriaScoresUseCase(
    private val repository: CriteriaRepository,
) {

    suspend operator fun invoke(
        taskAnswerId: String,
    ) = repository.getTaskAnswerCriteriaScores(taskAnswerId)
}
