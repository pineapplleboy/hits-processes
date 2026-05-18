package com.example.googleclass.feature.criteria.data.mapper

import com.example.googleclass.feature.criteria.data.model.CriteriaScoreRequestDto
import com.example.googleclass.feature.criteria.data.model.EvaluationFunctionDto
import com.example.googleclass.feature.criteria.data.model.MarkCriteriaDto
import com.example.googleclass.feature.criteria.data.model.MarkCriteriaWriteRequestDto
import com.example.googleclass.feature.criteria.data.model.TaskAnswerCriteriaScoreDto
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.EvaluationFunction
import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore

fun MarkCriteriaDto.toDomain(): EvaluationCriterion = EvaluationCriterion(
    id = id,
    name = name,
    evaluationFunction = evaluationFunction.toDomain(),
    multiplier = multiplier,
    minScore = minScore,
    maxScore = maxScore,
    postId = postId,
)

fun TaskAnswerCriteriaScoreDto.toDomain(): TaskAnswerCriteriaScore = TaskAnswerCriteriaScore(
    markCriteriaId = markCriteriaId,
    name = name,
    score = score,
    minScore = minScore,
    maxScore = maxScore,
    multiplier = multiplier,
)

fun MarkCriteriaDraft.toDto(): MarkCriteriaWriteRequestDto = MarkCriteriaWriteRequestDto(
    name = name,
    minScore = minScore,
    maxScore = maxScore,
    multiplier = multiplier,
)

fun CriteriaScoreDraft.toDto(): CriteriaScoreRequestDto = CriteriaScoreRequestDto(
    markCriteriaId = markCriteriaId,
    score = score,
)

private fun EvaluationFunctionDto.toDomain(): EvaluationFunction = when (this) {
    EvaluationFunctionDto.SUM -> EvaluationFunction.SUM
    EvaluationFunctionDto.MULTIPLY -> EvaluationFunction.MULTIPLY
}
