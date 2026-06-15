package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class SubmitAppraiserCriteriaScoresUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(appraiserId: String, scores: List<CriteriaScoreDraft>) =
        repository.putAppraiserCriteriaScores(appraiserId, scores)
}
