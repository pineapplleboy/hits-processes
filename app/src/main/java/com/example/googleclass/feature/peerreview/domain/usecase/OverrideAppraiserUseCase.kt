package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class OverrideAppraiserUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(appraiserId: String, score: Float) =
        repository.overrideAppraiserEvaluation(appraiserId, score)
}
