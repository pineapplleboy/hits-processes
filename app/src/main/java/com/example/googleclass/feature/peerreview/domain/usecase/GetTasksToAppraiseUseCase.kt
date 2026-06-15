package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class GetTasksToAppraiseUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(postId: String?) = repository.getTasksToAppraise(postId)
}
