package com.example.googleclass.feature.peerreview.presentation.top

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.peerreview.domain.usecase.GetAppraisersTopUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppraisersTopViewModel(
    private val courseId: String,
    private val getAppraisersTopUseCase: GetAppraisersTopUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppraisersTopUiState>(AppraisersTopUiState.Loading)
    val uiState: StateFlow<AppraisersTopUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = AppraisersTopUiState.Loading
            getAppraisersTopUseCase(courseId)
                .onSuccess { entries ->
                    _uiState.value = AppraisersTopUiState.Content(
                        entries = entries.sortedWith(
                            compareByDescending<com.example.googleclass.feature.peerreview.domain.model.AppraiserTopEntry> { it.appraisedNumber }
                                .thenByDescending { it.matchPercentage },
                        ),
                    )
                }
                .onFailure {
                    _uiState.value = AppraisersTopUiState.Error(
                        it.message ?: "Не удалось загрузить топ оценщиков",
                    )
                }
        }
    }
}
