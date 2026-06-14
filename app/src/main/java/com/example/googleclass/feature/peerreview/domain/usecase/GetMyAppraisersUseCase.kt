package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class GetMyAppraisersUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(taskAnswerId: String) =
        repository.getTaskAnswerAppraisers(taskAnswerId)
}
