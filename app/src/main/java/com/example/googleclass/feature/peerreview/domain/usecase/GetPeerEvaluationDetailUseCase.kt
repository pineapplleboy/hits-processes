package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class GetPeerEvaluationDetailUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(evaluationId: String) =
        repository.getPeerEvaluationDetail(evaluationId)
}
