package com.example.googleclass.feature.peerreview.presentation.top

import com.example.googleclass.feature.peerreview.domain.model.AppraiserTopEntry

sealed interface AppraisersTopUiState {
    data object Loading : AppraisersTopUiState
    data class Content(val entries: List<AppraiserTopEntry>) : AppraisersTopUiState
    data class Error(val message: String) : AppraisersTopUiState
}
