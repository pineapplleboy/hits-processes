package com.example.googleclass.feature.criteria.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.usecase.GetTaskAnswerCriteriaScoresUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutCriteriaScoreUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutSelfAssessmentCriteriaScoreUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CriteriaScoresViewModel(
    private val taskAnswerId: String,
    private val isSelfAssessment: Boolean,
    private val getTaskAnswerCriteriaScoresUseCase: GetTaskAnswerCriteriaScoresUseCase,
    private val putCriteriaScoreUseCase: PutCriteriaScoreUseCase,
    private val putSelfAssessmentCriteriaScoreUseCase: PutSelfAssessmentCriteriaScoreUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CriteriaScoresUiState>(CriteriaScoresUiState.Loading)
    val uiState: StateFlow<CriteriaScoresUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableStateFlow<CriteriaScoresUiEffect>(CriteriaScoresUiEffect.None)
    val uiEffect: StateFlow<CriteriaScoresUiEffect> = _uiEffect.asStateFlow()

    init {
        if (taskAnswerId.isNotBlank()) {
            loadScores(showLoading = true)
        }
    }

    fun onEvent(event: CriteriaScoresUiEvent) {
        when (event) {
            is CriteriaScoresUiEvent.ScoreChanged -> updateScoreInput(
                markCriteriaId = event.markCriteriaId,
                value = event.value,
            )

            is CriteriaScoresUiEvent.SaveScore -> saveScore(event.markCriteriaId)
        }
    }

    fun consumeEffect() {
        _uiEffect.value = CriteriaScoresUiEffect.None
    }

    private fun loadScores(showLoading: Boolean = false) {
        if (showLoading) {
            _uiState.value = CriteriaScoresUiState.Loading
        } else {
            updateContent { copy(isRefreshing = true) }
        }

        viewModelScope.launch {
            getTaskAnswerCriteriaScoresUseCase(taskAnswerId)
                .onSuccess { scores ->
                    _uiState.value = CriteriaScoresUiState.Content(
                        scores = scores.map { criterion ->
                            CriteriaScoreFieldState(
                                criterion = criterion,
                                input = formatDecimal(criterion.score),
                                isSaving = false,
                            )
                        },
                        isRefreshing = false,
                    )
                }
                .onFailure {
                    _uiState.value = CriteriaScoresUiState.Content(
                        scores = emptyList(),
                        isRefreshing = false,
                    )
                    sendEffect(
                        CriteriaScoresUiEffect.ShowMessage(
                            it.message ?: "Не удалось загрузить оценки по критериям",
                        ),
                    )
                }
        }
    }

    private fun updateScoreInput(markCriteriaId: String, value: String) {
        updateContent {
            copy(
                scores = scores.map { state ->
                    if (state.criterion.markCriteriaId == markCriteriaId) {
                        state.copy(
                            input = value.filter {
                                it.isDigit() || it == '.' || it == ',' || it == '-'
                            },
                        )
                    } else {
                        state
                    }
                },
            )
        }
    }

    private fun saveScore(markCriteriaId: String) {
        val state = _uiState.value as? CriteriaScoresUiState.Content ?: return
        val fieldState = state.scores.firstOrNull { it.criterion.markCriteriaId == markCriteriaId } ?: return
        if (fieldState.isSaving) return

        val score = fieldState.input.toNormalizedFloatOrNull()
        if (score == null) {
            sendEffect(CriteriaScoresUiEffect.ShowMessage("Введите корректную оценку"))
            return
        }
        if (score < fieldState.criterion.minScore || score > fieldState.criterion.maxScore) {
            sendEffect(
                CriteriaScoresUiEffect.ShowMessage(
                    "Оценка должна быть в диапазоне от ${formatDecimal(fieldState.criterion.minScore)} до ${formatDecimal(fieldState.criterion.maxScore)}",
                ),
            )
            return
        }

        updateContent {
            copy(
                scores = scores.map {
                    if (it.criterion.markCriteriaId == markCriteriaId) it.copy(isSaving = true) else it
                },
            )
        }

        viewModelScope.launch {
            val draft = CriteriaScoreDraft(
                markCriteriaId = markCriteriaId,
                score = score,
            )

            val result = if (isSelfAssessment) {
                putSelfAssessmentCriteriaScoreUseCase(taskAnswerId, draft)
            } else {
                putCriteriaScoreUseCase(taskAnswerId, draft)
            }

            result
                .onSuccess {
                    updateContent {
                        copy(
                            scores = scores.map {
                                if (it.criterion.markCriteriaId == markCriteriaId) {
                                    it.copy(
                                        criterion = it.criterion.copy(score = score),
                                        input = formatDecimal(score),
                                        isSaving = false,
                                    )
                                } else {
                                    it
                                }
                            },
                        )
                    }
                    sendEffect(CriteriaScoresUiEffect.ShowMessage("Оценка сохранена"))
                }
                .onFailure {
                    updateContent {
                        copy(
                            scores = scores.map {
                                if (it.criterion.markCriteriaId == markCriteriaId) {
                                    it.copy(isSaving = false)
                                } else {
                                    it
                                }
                            },
                        )
                    }
                    sendEffect(
                        CriteriaScoresUiEffect.ShowMessage(
                            it.message ?: "Не удалось сохранить оценку по критерию",
                        ),
                    )
                }
        }
    }

    private inline fun updateContent(
        transform: CriteriaScoresUiState.Content.() -> CriteriaScoresUiState.Content,
    ) {
        val state = _uiState.value
        if (state is CriteriaScoresUiState.Content) {
            _uiState.value = state.transform()
        }
    }

    private fun sendEffect(effect: CriteriaScoresUiEffect) {
        _uiEffect.value = effect
    }

    private fun String.toNormalizedFloatOrNull(): Float? = replace(',', '.').toFloatOrNull()

    private fun formatDecimal(value: Float): String =
        if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
}
