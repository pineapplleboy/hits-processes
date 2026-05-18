package com.example.googleclass.feature.criteria.presentation

import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore

sealed interface CriteriaScoresUiState {
    data object Loading : CriteriaScoresUiState

    data class Content(
        val scores: List<CriteriaScoreFieldState>,
        val isRefreshing: Boolean,
    ) : CriteriaScoresUiState
}

data class CriteriaScoreFieldState(
    val criterion: TaskAnswerCriteriaScore,
    val input: String,
    val isSaving: Boolean,
)

sealed interface CriteriaScoresUiEvent {
    data class ScoreChanged(
        val markCriteriaId: String,
        val value: String,
    ) : CriteriaScoresUiEvent

    data class SaveScore(
        val markCriteriaId: String,
    ) : CriteriaScoresUiEvent
}

sealed interface CriteriaScoresUiEffect {
    data object None : CriteriaScoresUiEffect
    data class ShowMessage(val message: String) : CriteriaScoresUiEffect
}
