package com.example.googleclass.feature.peerreview.domain.usecase

import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class GetAppraisersTopUseCase(
    private val repository: PeerReviewRepository,
) {

    suspend operator fun invoke(courseId: String) = repository.getCourseAppraisersTop(courseId)
}
