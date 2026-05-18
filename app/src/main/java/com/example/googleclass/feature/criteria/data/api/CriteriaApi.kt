package com.example.googleclass.feature.criteria.data.api

import com.example.googleclass.common.network.dto.IdResponseDto
import com.example.googleclass.feature.criteria.data.model.CriteriaScoreRequestDto
import com.example.googleclass.feature.criteria.data.model.MarkCriteriaDto
import com.example.googleclass.feature.criteria.data.model.MarkCriteriaWriteRequestDto
import com.example.googleclass.feature.criteria.data.model.TaskAnswerCriteriaScoreDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CriteriaApi {

    @GET(MARK_CRITERIA)
    suspend fun getMarkCriteria(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String,
    ): Response<List<MarkCriteriaDto>>

    @POST(MARK_CRITERIA)
    suspend fun createMarkCriteria(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String,
        @Body body: MarkCriteriaWriteRequestDto,
    ): Response<IdResponseDto>

    @PUT(MARK_CRITERIA_BY_ID)
    suspend fun updateMarkCriteria(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String,
        @Path("markCriteriaId") markCriteriaId: String,
        @Body body: MarkCriteriaWriteRequestDto,
    ): Response<Unit>

    @DELETE(MARK_CRITERIA_BY_ID)
    suspend fun deleteMarkCriteria(
        @Path("courseId") courseId: String,
        @Path("postId") postId: String,
        @Path("markCriteriaId") markCriteriaId: String,
    ): Response<Unit>

    @GET(TASK_ANSWER_CRITERIA_SCORES)
    suspend fun getTaskAnswerCriteriaScores(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<List<TaskAnswerCriteriaScoreDto>>

    @PUT(TASK_ANSWER_CRITERIA_SCORES)
    suspend fun putCriteriaScore(
        @Path("taskAnswerId") taskAnswerId: String,
        @Body body: CriteriaScoreRequestDto,
    ): Response<Unit>

    @PUT(SELF_ASSESSMENT_CRITERIA_SCORES)
    suspend fun putSelfAssessmentCriteriaScore(
        @Path("taskAnswerId") taskAnswerId: String,
        @Body body: CriteriaScoreRequestDto,
    ): Response<Unit>

    private companion object {
        const val MARK_CRITERIA = "api/v1/courses/{courseId}/posts/{postId}/mark-criteria"
        const val MARK_CRITERIA_BY_ID =
            "api/v1/courses/{courseId}/posts/{postId}/mark-criteria/{markCriteriaId}"
        const val TASK_ANSWER_CRITERIA_SCORES =
            "api/v1/task-answer/task-answer/{taskAnswerId}/criteria-scores"
        const val SELF_ASSESSMENT_CRITERIA_SCORES =
            "api/v1/task-answer/task-answer/{taskAnswerId}/self-assessment/criteria-scores"
    }
}
