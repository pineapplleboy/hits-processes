package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class SubmitAppraiserScoreUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(appraiserId: String, score: Float) =
        repository.submitAppraiserEvaluation(appraiserId, score)
}
