package com.example.googleclass.feature.taskdetail.domain.repository

import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswer
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile

interface TaskAnswerRepository {

    suspend fun getAllUserTaskAnswers(): Result<List<TaskAnswer>>

    suspend fun getUserPostTaskAnswer(postId: String): Result<TaskAnswer?>

    suspend fun getAllPostTaskAnswers(postId: String): Result<List<TaskAnswer>>

    suspend fun evaluateTask(taskAnswerId: String, score: Int): Result<Unit>

    suspend fun submitTask(taskAnswerId: String): Result<Unit>

    suspend fun unsubmitTask(taskAnswerId: String): Result<Unit>

    suspend fun appendFiles(taskAnswerId: String, files: List<TaskAnswerFile>): Result<Unit>

    suspend fun unpinFile(taskAnswerId: String, fileId: String): Result<Unit>
}
