package com.example.googleclass.feature.criteria.presentation

import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion

sealed interface CriteriaUiState {
    data object Loading : CriteriaUiState

    data class Content(
        val criteria: List<EvaluationCriterion>,
        val isSaving: Boolean,
    ) : CriteriaUiState
}

sealed interface CriteriaUiEvent {
    data object NavigateBack : CriteriaUiEvent
    data object Save : CriteriaUiEvent
    data object AddCriterion : CriteriaUiEvent
    data class EditCriterion(val criterionId: String) : CriteriaUiEvent
    data class DeleteCriterion(val criterionId: String) : CriteriaUiEvent
}

sealed interface CriteriaUiEffect {
    data object None : CriteriaUiEffect
    data object NavigateBack : CriteriaUiEffect
    data class ShowMessage(val message: String) : CriteriaUiEffect
}
