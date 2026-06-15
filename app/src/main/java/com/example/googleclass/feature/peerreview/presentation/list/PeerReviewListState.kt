package com.example.googleclass.feature.peerreview.presentation.list

import com.example.googleclass.feature.peerreview.domain.model.AvailableWork
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation

sealed interface PeerReviewListUiState {

    data object Loading : PeerReviewListUiState

    data class Content(
        val assigned: List<PeerEvaluation>,
        val available: List<AvailableWork>,
        val isRefreshing: Boolean = false,
        val openingTaskAnswerId: String? = null,
    ) : PeerReviewListUiState

    data class Error(val message: String) : PeerReviewListUiState
}

sealed interface PeerReviewListUiEvent {
    data object NavigateBack : PeerReviewListUiEvent
    data object Refresh : PeerReviewListUiEvent

    /** Открыть уже назначенную работу (режим CHAIN) — id оценивания известен. */
    data class OpenEvaluation(val evaluationId: String) : PeerReviewListUiEvent

    /** Открыть выбранную работу (режим ANY) — при необходимости берём её на оценку. */
    data class OpenWork(val taskAnswerId: String) : PeerReviewListUiEvent
}

sealed interface PeerReviewListUiEffect {
    data object NavigateBack : PeerReviewListUiEffect
    data class NavigateToEvaluation(val evaluationId: String) : PeerReviewListUiEffect
    data class ShowMessage(val message: String) : PeerReviewListUiEffect
}
