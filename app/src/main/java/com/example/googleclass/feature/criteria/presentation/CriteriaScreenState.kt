package com.example.googleclass.feature.criteria.presentation

import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion

sealed interface CriteriaUiState {
    data object Loading : CriteriaUiState

    data class Content(
        val criteria: List<EvaluationCriterion>,
        val allowsMultiplier: Boolean,
        val isRefreshing: Boolean,
        val editor: CriterionEditorState? = null,
        val pendingDelete: EvaluationCriterion? = null,
    ) : CriteriaUiState
}

data class CriterionEditorState(
    val mode: CriterionEditorMode,
    val criterionId: String? = null,
    val name: String = "",
    val isPassFail: Boolean = false,
    val minScore: String = "0",
    val maxScore: String = "",
    val allowsMultiplier: Boolean = false,
    val multiplier: String = "",
    val isSaving: Boolean = false,
)

enum class CriterionEditorMode {
    CREATE,
    EDIT,
}

sealed interface CriteriaUiEvent {
    data object NavigateBack : CriteriaUiEvent
    data object Retry : CriteriaUiEvent
    data object AddCriterion : CriteriaUiEvent
    data object DismissEditor : CriteriaUiEvent
    data object SaveCriterion : CriteriaUiEvent
    data object DismissDelete : CriteriaUiEvent
    data object ConfirmDelete : CriteriaUiEvent
    data class EditCriterion(val criterionId: String) : CriteriaUiEvent
    data class RequestDeleteCriterion(val criterionId: String) : CriteriaUiEvent
    data class NameChanged(val value: String) : CriteriaUiEvent
    data class PassFailChanged(val enabled: Boolean) : CriteriaUiEvent
    data class MinScoreChanged(val value: String) : CriteriaUiEvent
    data class MaxScoreChanged(val value: String) : CriteriaUiEvent
    data class MultiplierChanged(val value: String) : CriteriaUiEvent
}

sealed interface CriteriaUiEffect {
    data object NavigateBack : CriteriaUiEffect
    data class ShowMessage(val message: String) : CriteriaUiEffect
}
