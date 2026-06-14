package com.example.googleclass.feature.peerreview.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.peerreview.domain.usecase.GetAvailableWorksUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetTasksToAppraiseUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SelectWorkToAppraiseUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeerReviewListViewModel(
    private val courseId: String,
    private val postId: String,
    private val getTasksToAppraiseUseCase: GetTasksToAppraiseUseCase,
    private val getAvailableWorksUseCase: GetAvailableWorksUseCase,
    private val selectWorkToAppraiseUseCase: SelectWorkToAppraiseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PeerReviewListUiState>(PeerReviewListUiState.Loading)
    val uiState: StateFlow<PeerReviewListUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PeerReviewListUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        load(showLoading = true)
    }

    fun onEvent(event: PeerReviewListUiEvent) {
        when (event) {
            PeerReviewListUiEvent.NavigateBack -> _uiEffect.tryEmit(PeerReviewListUiEffect.NavigateBack)
            PeerReviewListUiEvent.Refresh -> load(showLoading = false)
            is PeerReviewListUiEvent.SelectWork -> selectWork(event.taskAnswerId)
            is PeerReviewListUiEvent.OpenEvaluation ->
                _uiEffect.tryEmit(PeerReviewListUiEffect.NavigateToEvaluation(event.evaluationId))
        }
    }

    private fun load(showLoading: Boolean) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = PeerReviewListUiState.Loading
            } else {
                (_uiState.value as? PeerReviewListUiState.Content)?.let {
                    _uiState.value = it.copy(isRefreshing = true)
                }
            }

            val assignedDeferred = async { getTasksToAppraiseUseCase(postId) }
            val availableDeferred = async { getAvailableWorksUseCase(postId) }

            val assignedResult = assignedDeferred.await()
            val availableResult = availableDeferred.await()

            // Доступные работы есть только для свободного выбора; для режима «по цепочке»
            // эндпоинт может вернуть ошибку — в этом случае показываем только назначенные.
            val assigned = assignedResult.getOrNull()
            if (assigned == null && availableResult.isFailure) {
                _uiState.value = PeerReviewListUiState.Error(
                    assignedResult.exceptionOrNull()?.message
                        ?: "Не удалось загрузить работы для оценивания",
                )
                return@launch
            }

            _uiState.value = PeerReviewListUiState.Content(
                assigned = assigned.orEmpty(),
                available = availableResult.getOrNull().orEmpty(),
                isRefreshing = false,
                selectingTaskAnswerId = null,
            )
        }
    }

    private fun selectWork(taskAnswerId: String) {
        val state = _uiState.value as? PeerReviewListUiState.Content ?: return
        if (state.selectingTaskAnswerId != null) return
        _uiState.value = state.copy(selectingTaskAnswerId = taskAnswerId)

        viewModelScope.launch {
            selectWorkToAppraiseUseCase(taskAnswerId)
                .onSuccess {
                    _uiEffect.tryEmit(PeerReviewListUiEffect.ShowMessage("Работа взята на оценку"))
                    load(showLoading = false)
                }
                .onFailure {
                    val current = _uiState.value as? PeerReviewListUiState.Content
                    if (current != null) {
                        _uiState.value = current.copy(selectingTaskAnswerId = null)
                    }
                    _uiEffect.tryEmit(
                        PeerReviewListUiEffect.ShowMessage(
                            it.message ?: "Не удалось взять работу на оценку",
                        ),
                    )
                }
        }
    }
}
