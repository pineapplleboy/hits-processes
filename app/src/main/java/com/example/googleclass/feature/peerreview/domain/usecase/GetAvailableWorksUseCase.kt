package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class GetAvailableWorksUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(postId: String) = repository.getAvailableWorksToAppraise(postId)
}
