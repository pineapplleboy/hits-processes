package com.example.googleclass.feature.taskdetail.data.mapper

import com.example.googleclass.feature.courses.data.remote.TaskAnswerDto
import com.example.googleclass.feature.taskdetail.data.model.FileModel
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswer
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile

private fun TaskAnswerDto.resolveUserName(): String? {
    if (!userName.isNullOrBlank()) return userName
    val u = user ?: userModel ?: author ?: return null
    return "${u.firstName.orEmpty()} ${u.lastName.orEmpty()}".trim().takeIf { it.isNotBlank() }
}

fun TaskAnswerDto.toTaskAnswer(): TaskAnswer = TaskAnswer(
    id = id,
    score = score,
    submittedAt = submittedAt,
    status = status,
    files = files.map { it.toTaskAnswerFile() },
    maxScore = maxScore,
    postName = postName,
    userId = userId ?: user?.id ?: userModel?.id ?: author?.id,
    userName = resolveUserName(),
)

fun FileModel.toTaskAnswerFile(): TaskAnswerFile = TaskAnswerFile(
    id = id,
    fileName = fileName,
)

fun TaskAnswerFile.toFileModel(): FileModel = FileModel(
    id = id,
    fileName = fileName,
)
