package com.example.googleclass.feature.taskdetail.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallNullable
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.courses.data.remote.TaskAnswerApi
import com.example.googleclass.feature.courses.data.remote.TaskRateRequestDto
import com.example.googleclass.feature.taskdetail.data.mapper.toFileModel
import com.example.googleclass.feature.taskdetail.data.mapper.toTaskAnswer
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswer
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile
import com.example.googleclass.feature.taskdetail.domain.repository.TaskAnswerRepository

class TaskAnswerRepositoryImpl(
    private val taskAnswerApi: TaskAnswerApi,
) : TaskAnswerRepository {

    override suspend fun getAllUserTaskAnswers(): Result<List<TaskAnswer>> = safeApiCall(
        apiCall = { taskAnswerApi.getAllUserTaskAnswers() },
        converter = { dtos -> dtos.map { it.toTaskAnswer() } },
    )

    override suspend fun getUserPostTaskAnswer(postId: String): Result<TaskAnswer?> = safeApiCallNullable(
        apiCall = { taskAnswerApi.getUserPostTaskAnswer(postId) },
        converter = { it.toTaskAnswer() },
    )

    override suspend fun getAllPostTaskAnswers(postId: String): Result<List<TaskAnswer>> = safeApiCall(
        apiCall = { taskAnswerApi.getAllPostTaskAnswers(postId) },
        converter = { dtos -> dtos.map { it.toTaskAnswer() } },
    )

    override suspend fun evaluateTask(taskAnswerId: String, score: Int): Result<Unit> = safeApiCallUnit(
        apiCall = { taskAnswerApi.evaluateTask(taskAnswerId, TaskRateRequestDto(rate = score)) },
    )

    override suspend fun submitTask(taskAnswerId: String): Result<Unit> = safeApiCallUnit(
        apiCall = { taskAnswerApi.submitTask(taskAnswerId) },
    )

    override suspend fun unsubmitTask(taskAnswerId: String): Result<Unit> = safeApiCallUnit(
        apiCall = { taskAnswerApi.unsubmitTask(taskAnswerId) },
    )

    override suspend fun appendFiles(taskAnswerId: String, files: List<TaskAnswerFile>): Result<Unit> =
        safeApiCallUnit(
            apiCall = {
                taskAnswerApi.appendFiles(
                    taskAnswerId = taskAnswerId,
                    files = files.map { it.toFileModel() },
                )
            },
        )

    override suspend fun unpinFile(taskAnswerId: String, fileId: String): Result<Unit> = safeApiCallUnit(
        apiCall = { taskAnswerApi.unpinFile(taskAnswerId, fileId) },
    )
}
