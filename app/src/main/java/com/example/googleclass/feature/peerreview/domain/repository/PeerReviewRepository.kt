package com.example.googleclass.feature.peerreview.domain.repository

import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.peerreview.domain.model.AppraiserTopEntry
import com.example.googleclass.feature.peerreview.domain.model.AvailableWork
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation

interface PeerReviewRepository {

    suspend fun getTasksToAppraise(postId: String?): Result<List<PeerEvaluation>>

    suspend fun getAvailableWorksToAppraise(postId: String): Result<List<AvailableWork>>

    suspend fun selectWorkToAppraise(taskAnswerId: String): Result<Unit>

    suspend fun getPeerEvaluationDetail(evaluationId: String): Result<PeerEvaluation>

    suspend fun submitAppraiserEvaluation(appraiserId: String, score: Float): Result<Unit>

    suspend fun putAppraiserCriteriaScores(
        appraiserId: String,
        scores: List<CriteriaScoreDraft>,
    ): Result<Unit>

    suspend fun getTaskAnswerAppraisers(taskAnswerId: String): Result<List<PeerEvaluation>>

    suspend fun getAllTaskAnswerAppraisers(taskAnswerId: String): Result<List<PeerEvaluation>>

    suspend fun overrideAppraiserEvaluation(appraiserId: String, score: Float): Result<Unit>

    suspend fun getCourseAppraisersTop(courseId: String): Result<List<AppraiserTopEntry>>
}
