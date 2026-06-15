package com.example.googleclass.feature.peerreview.data.api

import com.example.googleclass.feature.courses.data.remote.TaskRateRequestDto
import com.example.googleclass.feature.criteria.data.model.CriteriaScoreRequestDto
import com.example.googleclass.feature.peerreview.data.model.AppraiserTopCourseDto
import com.example.googleclass.feature.peerreview.data.model.AvailablePeerEvaluationDto
import com.example.googleclass.feature.peerreview.data.model.PeerEvaluationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PeerReviewApi {

    @GET(TASKS_TO_APPRAISE)
    suspend fun getTasksToAppraise(
        @Query("postId") postId: String?,
    ): Response<List<PeerEvaluationDto>>

    @GET(AVAILABLE_TO_APPRAISE)
    suspend fun getAvailableWorksToAppraise(
        @Path("postId") postId: String,
    ): Response<List<AvailablePeerEvaluationDto>>

    @POST(SELECT_TO_APPRAISE)
    suspend fun selectWorkToAppraise(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<Unit>

    @GET(PEER_EVALUATION_DETAIL)
    suspend fun getPeerEvaluationDetail(
        @Path("evaluationId") evaluationId: String,
    ): Response<PeerEvaluationDto>

    @POST(APPRAISER_EVALUATE)
    suspend fun submitAppraiserEvaluation(
        @Path("appraiserId") appraiserId: String,
        @Body body: TaskRateRequestDto,
    ): Response<Unit>

    @PUT(APPRAISER_CRITERIA_SCORES)
    suspend fun putAppraiserCriteriaScores(
        @Path("appraiserId") appraiserId: String,
        @Body body: List<CriteriaScoreRequestDto>,
    ): Response<Unit>

    @GET(TASK_ANSWER_APPRAISERS)
    suspend fun getTaskAnswerAppraisers(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<List<PeerEvaluationDto>>

    @GET(TASK_ANSWER_APPRAISERS_ALL)
    suspend fun getAllTaskAnswerAppraisers(
        @Path("taskAnswerId") taskAnswerId: String,
    ): Response<List<PeerEvaluationDto>>

    @PUT(APPRAISER_OVERRIDE)
    suspend fun overrideAppraiserEvaluation(
        @Path("appraiserId") appraiserId: String,
        @Body body: TaskRateRequestDto,
    ): Response<Unit>

    @GET(COURSE_APPRAISERS_TOP)
    suspend fun getCourseAppraisersTop(
        @Path("courseId") courseId: String,
    ): Response<List<AppraiserTopCourseDto>>

    private companion object {
        const val TASKS_TO_APPRAISE = "api/v1/task-answer/to-appraise"
        const val AVAILABLE_TO_APPRAISE = "api/v1/task-answer/post/{postId}/available-to-appraise"
        const val SELECT_TO_APPRAISE =
            "api/v1/task-answer/task-answer/{taskAnswerId}/select-to-appraise"
        const val PEER_EVALUATION_DETAIL = "api/v1/task-answer/peer-evaluation/{evaluationId}"
        const val APPRAISER_EVALUATE = "api/v1/task-answer/appraiser/{appraiserId}/evaluate"
        const val APPRAISER_CRITERIA_SCORES =
            "api/v1/task-answer/appraiser/{appraiserId}/criteria-scores"
        const val TASK_ANSWER_APPRAISERS =
            "api/v1/task-answer/task-answer/{taskAnswerId}/appraisers"
        const val TASK_ANSWER_APPRAISERS_ALL =
            "api/v1/task-answer/task-answer/{taskAnswerId}/appraisers/all"
        const val APPRAISER_OVERRIDE = "api/v1/task-answer/appraiser/{appraiserId}/override"
        const val COURSE_APPRAISERS_TOP = "api/v1/courses/{courseId}/appraisers-top"
    }
}
