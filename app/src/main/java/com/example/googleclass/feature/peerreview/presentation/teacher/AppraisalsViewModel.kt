package com.example.googleclass.feature.peerreview.presentation.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.peerreview.domain.usecase.GetAllAppraisersUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.OverrideAppraiserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AppraisalsViewModel(
    private val taskAnswerId: String,
    private val getAllAppraisersUseCase: GetAllAppraisersUseCase,
    private val overrideAppraiserUseCase: OverrideAppraiserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppraisalsUiState>(AppraisalsUiState.Loading)
    val uiState: StateFlow<AppraisalsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AppraisalsUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        load(showLoading = true)
    }

    fun onEvent(event: AppraisalsUiEvent) {
        when (event) {
            AppraisalsUiEvent.NavigateBack -> _uiEffect.tryEmit(AppraisalsUiEffect.NavigateBack)
            AppraisalsUiEvent.Refresh -> load(showLoading = false)
            is AppraisalsUiEvent.OpenOverride -> openOverride(event)
            is AppraisalsUiEvent.OverrideScoreChanged -> updateOverrideScore(event.value)
            AppraisalsUiEvent.SubmitOverride -> submitOverride()
            AppraisalsUiEvent.DismissOverride -> dismissOverride()
        }
    }

    private fun load(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = AppraisalsUiState.Loading
            } else {
                (_uiState.value as? AppraisalsUiState.Content)?.let {
                    _uiState.value = it.copy(isRefreshing = true)
                }
            }

            getAllAppraisersUseCase(taskAnswerId)
                .onSuccess { appraisers ->
                    _uiState.value = AppraisalsUiState.Content(
                        studentName = appraisers.firstOrNull()?.studentName,
                        appraisers = appraisers,
                        isRefreshing = false,
                        overrideDialog = null,
                    )
                }
                .onFailure {
                    _uiState.value = AppraisalsUiState.Error(
                        it.message ?: "Не удалось загрузить оценки",
                    )
                }
        }
    }

    private fun openOverride(event: AppraisalsUiEvent.OpenOverride) {
        val state = _uiState.value as? AppraisalsUiState.Content ?: return
        _uiState.value = state.copy(
            overrideDialog = OverrideDialogState(
                appraiserId = event.appraiserId,
                appraiserName = event.appraiserName,
                scoreInput = event.currentScore?.let(::formatScore).orEmpty(),
            ),
        )
    }

    private fun updateOverrideScore(value: String) {
        val state = _uiState.value as? AppraisalsUiState.Content ?: return
        val dialog = state.overrideDialog ?: return
        _uiState.value = state.copy(
            overrideDialog = dialog.copy(
                scoreInput = value.filter { it.isDigit() || it == '.' || it == ',' },
            ),
        )
    }

    private fun submitOverride() {
        val state = _uiState.value as? AppraisalsUiState.Content ?: return
        val dialog = state.overrideDialog ?: return
        if (dialog.isSaving) return

        val score = dialog.scoreInput.replace(',', '.').toFloatOrNull()
        if (score == null) {
            _uiEffect.tryEmit(AppraisalsUiEffect.ShowMessage("Введите корректную оценку"))
            return
        }

        _uiState.value = state.copy(overrideDialog = dialog.copy(isSaving = true))
        viewModelScope.launch {
            overrideAppraiserUseCase(dialog.appraiserId, score)
                .onSuccess {
                    _uiEffect.tryEmit(AppraisalsUiEffect.ShowMessage("Оценка переопределена"))
                    load(showLoading = false)
                }
                .onFailure {
                    val current = _uiState.value as? AppraisalsUiState.Content
                    if (current != null) {
                        _uiState.value = current.copy(
                            overrideDialog = dialog.copy(isSaving = false),
                        )
                    }
                    _uiEffect.tryEmit(
                        AppraisalsUiEffect.ShowMessage(it.message ?: "Не удалось переопределить оценку"),
                    )
                }
        }
    }

    private fun dismissOverride() {
        val state = _uiState.value as? AppraisalsUiState.Content ?: return
        _uiState.value = state.copy(overrideDialog = null)
    }

    private fun formatScore(value: Float): String =
        if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
}
