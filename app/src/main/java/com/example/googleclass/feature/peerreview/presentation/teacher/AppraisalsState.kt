package com.example.googleclass.feature.peerreview.presentation.teacher

import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation

sealed interface AppraisalsUiState {

    data object Loading : AppraisalsUiState

    data class Content(
        val studentName: String?,
        val appraisers: List<PeerEvaluation>,
        val isRefreshing: Boolean = false,
        val overrideDialog: OverrideDialogState? = null,
    ) : AppraisalsUiState

    data class Error(val message: String) : AppraisalsUiState
}

data class OverrideDialogState(
    val appraiserId: String,
    val appraiserName: String,
    val scoreInput: String,
    val isSaving: Boolean = false,
)

sealed interface AppraisalsUiEvent {
    data object NavigateBack : AppraisalsUiEvent
    data object Refresh : AppraisalsUiEvent
    data class OpenOverride(
        val appraiserId: String,
        val appraiserName: String,
        val currentScore: Float?,
    ) : AppraisalsUiEvent
    data class OverrideScoreChanged(val value: String) : AppraisalsUiEvent
    data object SubmitOverride : AppraisalsUiEvent
    data object DismissOverride : AppraisalsUiEvent
}

sealed interface AppraisalsUiEffect {
    data object NavigateBack : AppraisalsUiEffect
    data class ShowMessage(val message: String) : AppraisalsUiEffect
}
