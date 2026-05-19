package com.example.googleclass.feature.criteria.presentation

import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile

sealed interface CriteriaEvaluationUiState {
    data object Loading : CriteriaEvaluationUiState

    data class Content(
        val taskTitle: String,
        val studentName: String,
        val taskMaxScore: Float,
        val taskAnswerId: String,
        val evaluationType: TaskMarkEvaluationType?,
        val passThreshold: Float?,
        val files: List<TaskAnswerFile>,
        val comments: List<Comment>,
        val criteria: List<CriteriaEvaluationFieldState>,
        val calculatedScore: Float?,
        val isSaving: Boolean,
    ) : CriteriaEvaluationUiState
}

data class CriteriaEvaluationFieldState(
    val markCriteriaId: String,
    val name: String,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
    val input: String,
)

sealed interface CriteriaEvaluationUiEvent {
    data object NavigateBack : CriteriaEvaluationUiEvent
    data class ScoreChanged(
        val markCriteriaId: String,
        val value: String,
    ) : CriteriaEvaluationUiEvent
    data object Save : CriteriaEvaluationUiEvent
}

sealed interface CriteriaEvaluationUiEffect {
    data object NavigateBack : CriteriaEvaluationUiEffect
    data class ShowMessage(val message: String) : CriteriaEvaluationUiEffect
}
