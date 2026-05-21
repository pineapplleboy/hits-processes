package com.example.googleclass.feature.criteria.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore
import com.example.googleclass.feature.criteria.domain.usecase.GetMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetTaskAnswerCriteriaScoresUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutSelfAssessmentCriteriaScoreUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SelfAssessmentViewModel(
    private val courseId: String,
    private val postId: String,
    private val taskAnswerId: String,
    private val getMarkCriteriaUseCase: GetMarkCriteriaUseCase,
    private val getTaskAnswerCriteriaScoresUseCase: GetTaskAnswerCriteriaScoresUseCase,
    private val putSelfAssessmentCriteriaScoreUseCase: PutSelfAssessmentCriteriaScoreUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SelfAssessmentUiState>(SelfAssessmentUiState.Loading)
    val uiState: StateFlow<SelfAssessmentUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SelfAssessmentUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        load()
    }

    fun onEvent(event: SelfAssessmentUiEvent) {
        when (event) {
            is SelfAssessmentUiEvent.ScoreChanged -> updateScore(event.markCriteriaId, event.value)
            is SelfAssessmentUiEvent.SaveScore -> saveScore(event.markCriteriaId)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = SelfAssessmentUiState.Loading

            val criteriaDeferred = async { getMarkCriteriaUseCase(courseId, postId) }
            val scoresDeferred = async { getTaskAnswerCriteriaScoresUseCase(taskAnswerId) }

            val criteria = criteriaDeferred.await().getOrDefault(emptyList())
            val existingScores = scoresDeferred.await().getOrDefault(emptyList())

            val fields = mergeCriteriaWithScores(criteria, existingScores)

            _uiState.value = SelfAssessmentUiState.Content(
                fields = fields,
            )
        }
    }

    private fun mergeCriteriaWithScores(
        criteria: List<EvaluationCriterion>,
        existingScores: List<TaskAnswerCriteriaScore>,
    ): List<SelfAssessmentFieldState> {
        if (criteria.isEmpty() && existingScores.isEmpty()) return emptyList()

        val scoresById = existingScores.associateBy { it.markCriteriaId }

        return if (criteria.isNotEmpty()) {
            criteria.map { criterion ->
                val existingScore = scoresById[criterion.id]
                SelfAssessmentFieldState(
                    markCriteriaId = criterion.id,
                    name = criterion.name,
                    description = criterion.description ?: existingScore?.description,
                    minScore = criterion.minScore,
                    maxScore = criterion.maxScore,
                    multiplier = criterion.multiplier,
                    input = existingScore?.score?.let(::formatScore).orEmpty(),
                    isSaving = false,
                )
            }
        } else {
            existingScores.map { score ->
                SelfAssessmentFieldState(
                    markCriteriaId = score.markCriteriaId,
                    name = score.name,
                    description = score.description,
                    minScore = score.minScore,
                    maxScore = score.maxScore,
                    multiplier = score.multiplier,
                    input = score.score?.let(::formatScore).orEmpty(),
                    isSaving = false,
                )
            }
        }
    }

    private fun updateScore(markCriteriaId: String, value: String) {
        val state = _uiState.value as? SelfAssessmentUiState.Content ?: return
        _uiState.value = state.copy(
            fields = state.fields.map { field ->
                if (field.markCriteriaId == markCriteriaId) {
                    field.copy(input = value.filter { it.isDigit() || it == '.' || it == ',' || it == '-' })
                } else {
                    field
                }
            },
        )
    }

    private fun saveScore(markCriteriaId: String) {
        val state = _uiState.value as? SelfAssessmentUiState.Content ?: return
        val field = state.fields.firstOrNull { it.markCriteriaId == markCriteriaId } ?: return
        if (field.isSaving) return

        val score = field.input.replace(',', '.').toFloatOrNull()
        if (score == null) {
            _uiEffect.tryEmit(SelfAssessmentUiEffect.ShowMessage("Введите корректную оценку"))
            return
        }
        if (score < field.minScore || score > field.maxScore) {
            _uiEffect.tryEmit(
                SelfAssessmentUiEffect.ShowMessage(
                    "Оценка должна быть от ${formatScore(field.minScore)} до ${formatScore(field.maxScore)}"
                )
            )
            return
        }

        setFieldSaving(markCriteriaId, true)

        viewModelScope.launch {
            val draft = CriteriaScoreDraft(markCriteriaId = markCriteriaId, score = score)
            putSelfAssessmentCriteriaScoreUseCase(taskAnswerId, draft)
                .onSuccess {
                    updateFieldAfterSave(markCriteriaId, score)
                    _uiEffect.tryEmit(SelfAssessmentUiEffect.ShowMessage("Оценка сохранена"))
                }
                .onFailure {
                    setFieldSaving(markCriteriaId, false)
                    _uiEffect.tryEmit(
                        SelfAssessmentUiEffect.ShowMessage(
                            it.message ?: "Не удалось сохранить оценку"
                        )
                    )
                }
        }
    }

    private fun setFieldSaving(markCriteriaId: String, saving: Boolean) {
        val state = _uiState.value as? SelfAssessmentUiState.Content ?: return
        _uiState.value = state.copy(
            fields = state.fields.map {
                if (it.markCriteriaId == markCriteriaId) it.copy(isSaving = saving) else it
            },
        )
    }

    private fun updateFieldAfterSave(markCriteriaId: String, score: Float) {
        val state = _uiState.value as? SelfAssessmentUiState.Content ?: return
        _uiState.value = state.copy(
            fields = state.fields.map {
                if (it.markCriteriaId == markCriteriaId) {
                    it.copy(input = formatScore(score), isSaving = false)
                } else {
                    it
                }
            },
        )
    }

    private fun formatScore(value: Float): String =
        if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
}

sealed interface SelfAssessmentUiState {
    data object Loading : SelfAssessmentUiState

    data class Content(
        val fields: List<SelfAssessmentFieldState>,
    ) : SelfAssessmentUiState
}

data class SelfAssessmentFieldState(
    val markCriteriaId: String,
    val name: String,
    val description: String?,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
    val input: String,
    val isSaving: Boolean,
)

sealed interface SelfAssessmentUiEvent {
    data class ScoreChanged(val markCriteriaId: String, val value: String) : SelfAssessmentUiEvent
    data class SaveScore(val markCriteriaId: String) : SelfAssessmentUiEvent
}

sealed interface SelfAssessmentUiEffect {
    data class ShowMessage(val message: String) : SelfAssessmentUiEffect
}
