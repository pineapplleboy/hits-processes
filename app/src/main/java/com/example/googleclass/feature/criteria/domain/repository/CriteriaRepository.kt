package com.example.googleclass.feature.criteria.domain.repository

import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore

interface CriteriaRepository {

    suspend fun getMarkCriteria(
        courseId: String,
        postId: String,
    ): Result<List<EvaluationCriterion>>

    suspend fun createMarkCriteria(
        courseId: String,
        postId: String,
        draft: MarkCriteriaDraft,
    ): Result<String>

    suspend fun updateMarkCriteria(
        courseId: String,
        postId: String,
        markCriteriaId: String,
        draft: MarkCriteriaDraft,
    ): Result<Unit>

    suspend fun deleteMarkCriteria(
        courseId: String,
        postId: String,
        markCriteriaId: String,
    ): Result<Unit>

    suspend fun getTaskAnswerCriteriaScores(
        taskAnswerId: String,
    ): Result<List<TaskAnswerCriteriaScore>>

    suspend fun putCriteriaScore(
        taskAnswerId: String,
        draft: CriteriaScoreDraft,
    ): Result<Unit>

    suspend fun putSelfAssessmentCriteriaScore(
        taskAnswerId: String,
        draft: CriteriaScoreDraft,
    ): Result<Unit>
}
