package com.example.googleclass.feature.criteria.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.criteria.data.api.CriteriaApi
import com.example.googleclass.feature.criteria.data.mapper.toDomain
import com.example.googleclass.feature.criteria.data.mapper.toDto
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore
import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository

class CriteriaRepositoryImpl(
    private val criteriaApi: CriteriaApi,
) : CriteriaRepository {

    override suspend fun getMarkCriteria(
        courseId: String,
        postId: String,
    ): Result<List<EvaluationCriterion>> = safeApiCall(
        apiCall = { criteriaApi.getMarkCriteria(courseId, postId) },
        converter = { dtos -> dtos.map { it.toDomain() } },
    )

    override suspend fun createMarkCriteria(
        courseId: String,
        postId: String,
        draft: MarkCriteriaDraft,
    ): Result<String> = safeApiCall(
        apiCall = { criteriaApi.createMarkCriteria(courseId, postId, draft.toDto()) },
        converter = { it.id },
    )

    override suspend fun updateMarkCriteria(
        courseId: String,
        postId: String,
        markCriteriaId: String,
        draft: MarkCriteriaDraft,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { criteriaApi.updateMarkCriteria(courseId, postId, markCriteriaId, draft.toDto()) },
    )

    override suspend fun deleteMarkCriteria(
        courseId: String,
        postId: String,
        markCriteriaId: String,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { criteriaApi.deleteMarkCriteria(courseId, postId, markCriteriaId) },
    )

    override suspend fun getTaskAnswerCriteriaScores(
        taskAnswerId: String,
    ): Result<List<TaskAnswerCriteriaScore>> = safeApiCall(
        apiCall = { criteriaApi.getTaskAnswerCriteriaScores(taskAnswerId) },
        converter = { dtos -> dtos.map { it.toDomain() } },
    )

    override suspend fun putCriteriaScore(
        taskAnswerId: String,
        draft: CriteriaScoreDraft,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { criteriaApi.putCriteriaScore(taskAnswerId, draft.toDto()) },
    )

    override suspend fun putSelfAssessmentCriteriaScore(
        taskAnswerId: String,
        draft: CriteriaScoreDraft,
    ): Result<Unit> = safeApiCallUnit(
        apiCall = { criteriaApi.putSelfAssessmentCriteriaScore(taskAnswerId, draft.toDto()) },
    )
}
