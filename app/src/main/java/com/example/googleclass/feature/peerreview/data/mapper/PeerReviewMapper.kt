package com.example.googleclass.feature.peerreview.data.mapper

import com.example.googleclass.common.network.dto.UserDto
import com.example.googleclass.feature.peerreview.data.model.AppraiserTopCourseDto
import com.example.googleclass.feature.peerreview.data.model.AvailablePeerEvaluationDto
import com.example.googleclass.feature.peerreview.data.model.PeerEvaluationDto
import com.example.googleclass.feature.peerreview.data.model.ScoredMarkCriteriaDto
import com.example.googleclass.feature.peerreview.domain.model.AppraiserTopEntry
import com.example.googleclass.feature.peerreview.domain.model.AvailableWork
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation
import com.example.googleclass.feature.peerreview.domain.model.PeerReviewFile
import com.example.googleclass.feature.peerreview.domain.model.ScoredCriterion
import com.example.googleclass.feature.peerreview.domain.model.UnavailableReason
import com.example.googleclass.feature.taskdetail.data.model.FileDto

private fun UserDto.toDisplayName(): String? =
    "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().takeIf { it.isNotBlank() }

private fun FileDto.toPeerReviewFile(): PeerReviewFile = PeerReviewFile(
    id = id,
    fileName = fileName,
)

fun PeerEvaluationDto.toDomain(): PeerEvaluation = PeerEvaluation(
    id = id,
    taskAnswerId = taskAnswerId,
    studentId = student?.id,
    studentName = student?.toDisplayName(),
    appraiserId = appraiser?.id,
    appraiserName = appraiser?.toDisplayName(),
    score = score,
    submittedAt = submittedAt,
    criteriaScores = criteriaScores.map { it.toDomain() },
    files = files.map { it.toPeerReviewFile() },
)

fun ScoredMarkCriteriaDto.toDomain(): ScoredCriterion = ScoredCriterion(
    id = id,
    name = name,
    score = score,
    minScore = minScore,
    maxScore = maxScore,
    multiplier = multiplier,
)

fun AvailablePeerEvaluationDto.toDomain(): AvailableWork = AvailableWork(
    taskAnswerId = taskAnswerId,
    studentId = student?.id,
    studentName = student?.toDisplayName(),
    submittedAt = submittedAt,
    canAppraise = canAppraise,
    unavailableReason = UnavailableReason.fromRaw(unavailableReason),
    files = files.map { it.toPeerReviewFile() },
)

fun AppraiserTopCourseDto.toDomain(): AppraiserTopEntry = AppraiserTopEntry(
    studentId = studentModel?.id,
    studentName = studentModel?.toDisplayName(),
    appraisedNumber = appraisedNumber,
    matchPercentage = matchPercentage,
)
