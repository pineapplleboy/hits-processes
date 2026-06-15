package com.example.googleclass.feature.peerreview.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.courses.data.remote.TaskRateRequestDto
import com.example.googleclass.feature.criteria.data.mapper.toDto
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.peerreview.data.api.PeerReviewApi
import com.example.googleclass.feature.peerreview.data.mapper.toDomain
import com.example.googleclass.feature.peerreview.domain.model.AppraiserTopEntry
import com.example.googleclass.feature.peerreview.domain.model.AvailableWork
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation
import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository

class PeerReviewRepositoryImpl(
    private val peerReviewApi: PeerReviewApi,
) : PeerReviewRepository {

    override suspend fun getTasksToAppraise(postId: String?): Result<List<PeerEvaluation>> =
        safeApiCall(
            apiCall = { peerReviewApi.getTasksToAppraise(postId) },
            converter = { dtos -> dtos.map { it.toDomain() } },
        )

    override suspend fun getAvailableWorksToAppraise(postId: String): Result<List<AvailableWork>> =
        safeApiCall(
            apiCall = { peerReviewApi.getAvailableWorksToAppraise(postId) },
            converter = { dtos -> dtos.map { it.toDomain() } },
        )

    override suspend fun selectWorkToAppraise(taskAnswerId: String): Result<Unit> = safeApiCallUnit(
        apiCall = { peerReviewApi.selectWorkToAppraise(taskAnswerId) },
    )

    override suspend fun getPeerEvaluationDetail(evaluationId: String): Result<PeerEvaluation> =
        safeApiCall(
            apiCall = { peerReviewApi.getPeerEvaluationDetail(evaluationId) },
            converter = { it.toDomain() },
        )

    override suspend fun submitAppraiserEvaluation(
        appraiserId: String,
        score: Float,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { peerReviewApi.submitAppraiserEvaluation(appraiserId, TaskRateRequestDto(rate = score)) },
    )

    override suspend fun putAppraiserCriteriaScores(
        appraiserId: String,
        scores: List<CriteriaScoreDraft>,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { peerReviewApi.putAppraiserCriteriaScores(appraiserId, scores.map { it.toDto() }) },
    )

    override suspend fun getTaskAnswerAppraisers(
        taskAnswerId: String,
    ): Result<List<PeerEvaluation>> = safeApiCall(
        apiCall = { peerReviewApi.getTaskAnswerAppraisers(taskAnswerId) },
        converter = { dtos -> dtos.map { it.toDomain() } },
    )

    override suspend fun getAllTaskAnswerAppraisers(
        taskAnswerId: String,
    ): Result<List<PeerEvaluation>> = safeApiCall(
        apiCall = { peerReviewApi.getAllTaskAnswerAppraisers(taskAnswerId) },
        converter = { dtos -> dtos.map { it.toDomain() } },
    )

    override suspend fun overrideAppraiserEvaluation(
        appraiserId: String,
        score: Float,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { peerReviewApi.overrideAppraiserEvaluation(appraiserId, TaskRateRequestDto(rate = score)) },
    )

    override suspend fun getCourseAppraisersTop(courseId: String): Result<List<AppraiserTopEntry>> =
        safeApiCall(
            apiCall = { peerReviewApi.getCourseAppraisersTop(courseId) },
            converter = { dtos -> dtos.map { it.toDomain() } },
        )
}
