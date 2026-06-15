package com.example.googleclass.feature.peerreview.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.peerreview.domain.usecase.GetAvailableWorksUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetTasksToAppraiseUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SelectWorkToAppraiseUseCase
import com.example.googleclass.feature.post.data.model.TaskAnswerAppraisingType
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
    private val appraisingType: TaskAnswerAppraisingType?,
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
            is PeerReviewListUiEvent.OpenEvaluation ->
                _uiEffect.tryEmit(PeerReviewListUiEffect.NavigateToEvaluation(event.evaluationId))
            is PeerReviewListUiEvent.OpenWork -> openWork(event.taskAnswerId)
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

            when (appraisingType) {
                // По цепочке: работы назначаются автоматически — берём только /to-appraise.
                TaskAnswerAppraisingType.CHAIN -> {
                    getTasksToAppraiseUseCase(postId)
                        .onSuccess { assigned ->
                            _uiState.value = PeerReviewListUiState.Content(
                                assigned = assigned,
                                available = emptyList(),
                                isRefreshing = false,
                            )
                        }
                        .onFailure { showError(it) }
                }

                // Свободный выбор: студент сам берёт работы — берём /available-to-appraise.
                TaskAnswerAppraisingType.ANY -> {
                    getAvailableWorksUseCase(postId)
                        .onSuccess { available ->
                            _uiState.value = PeerReviewListUiState.Content(
                                assigned = emptyList(),
                                available = available,
                                isRefreshing = false,
                            )
                        }
                        .onFailure { showError(it) }
                }

                // Тип неизвестен — пробуем оба источника.
                null -> {
                    val assignedDeferred = async { getTasksToAppraiseUseCase(postId) }
                    val availableDeferred = async { getAvailableWorksUseCase(postId) }
                    val assignedResult = assignedDeferred.await()
                    val availableResult = availableDeferred.await()
                    val assigned = assignedResult.getOrNull()
                    if (assigned == null && availableResult.isFailure) {
                        showError(assignedResult.exceptionOrNull())
                        return@launch
                    }
                    _uiState.value = PeerReviewListUiState.Content(
                        assigned = assigned.orEmpty(),
                        available = availableResult.getOrNull().orEmpty(),
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    /**
     * Открывает работу для оценивания в режиме ANY: при необходимости берёт её на
     * оценку, затем находит id оценивания через /to-appraise и переходит на экран оценки.
     */
    private fun openWork(taskAnswerId: String) {
        val state = _uiState.value as? PeerReviewListUiState.Content ?: return
        if (state.openingTaskAnswerId != null) return
        val work = state.available.firstOrNull { it.taskAnswerId == taskAnswerId }
        _uiState.value = state.copy(openingTaskAnswerId = taskAnswerId)

        viewModelScope.launch {
            if (work?.canAppraise == true) {
                val selectResult = selectWorkToAppraiseUseCase(taskAnswerId)
                if (selectResult.isFailure) {
                    finishOpening()
                    _uiEffect.tryEmit(
                        PeerReviewListUiEffect.ShowMessage(
                            selectResult.exceptionOrNull()?.message
                                ?: "Не удалось взять работу на оценку",
                        ),
                    )
                    return@launch
                }
            }

            val evaluationId = getTasksToAppraiseUseCase(postId).getOrNull()
                ?.firstOrNull { it.taskAnswerId == taskAnswerId }
                ?.id

            finishOpening()
            if (evaluationId != null) {
                _uiEffect.tryEmit(PeerReviewListUiEffect.NavigateToEvaluation(evaluationId))
            } else {
                _uiEffect.tryEmit(
                    PeerReviewListUiEffect.ShowMessage("Не удалось открыть работу для оценки"),
                )
            }
        }
    }

    private fun finishOpening() {
        (_uiState.value as? PeerReviewListUiState.Content)?.let {
            _uiState.value = it.copy(openingTaskAnswerId = null)
        }
    }

    private fun showError(throwable: Throwable?) {
        _uiState.value = PeerReviewListUiState.Error(
            throwable?.message ?: "Не удалось загрузить работы для оценивания",
        )
    }
}
