package com.example.googleclass.feature.criteria.presentation

import androidx.lifecycle.ViewModel
import com.example.googleclass.feature.criteria.domain.model.CriterionGrading
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CriteriaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CriteriaUiState>(CriteriaUiState.Loading)
    val uiState: StateFlow<CriteriaUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableStateFlow<CriteriaUiEffect>(CriteriaUiEffect.None)
    val uiEffect: StateFlow<CriteriaUiEffect> = _uiEffect.asStateFlow()

    init {
        _uiState.value = CriteriaUiState.Content(
            criteria = previewCriteria,
            isSaving = false,
        )
    }

    fun onEvent(event: CriteriaUiEvent) {
        when (event) {
            CriteriaUiEvent.NavigateBack -> sendEffect(CriteriaUiEffect.NavigateBack)
            CriteriaUiEvent.Save -> {
                sendEffect(CriteriaUiEffect.ShowMessage("Сохранение критериев будет добавлено позже"))
            }
            CriteriaUiEvent.AddCriterion -> {
                sendEffect(CriteriaUiEffect.ShowMessage("Создание критерия будет добавлено позже"))
            }
            is CriteriaUiEvent.EditCriterion -> {
                sendEffect(CriteriaUiEffect.ShowMessage("Редактирование критерия будет добавлено позже"))
            }
            is CriteriaUiEvent.DeleteCriterion -> {
                updateContent { copy(criteria = criteria.filterNot { it.id == event.criterionId }) }
            }
        }
    }

    fun consumeEffect() {
        _uiEffect.value = CriteriaUiEffect.None
    }

    private inline fun updateContent(
        transform: CriteriaUiState.Content.() -> CriteriaUiState.Content,
    ) {
        val state = _uiState.value
        if (state is CriteriaUiState.Content) {
            _uiState.value = state.transform()
        }
    }

    private fun sendEffect(effect: CriteriaUiEffect) {
        _uiEffect.value = effect
    }

    private companion object {
        val previewCriteria = listOf(
            EvaluationCriterion(
                id = "compile",
                title = "Код компилируется",
                grading = CriterionGrading.PassFail,
            ),
            EvaluationCriterion(
                id = "classes",
                title = "Реализованы все классы",
                grading = CriterionGrading.PassFail,
            ),
            EvaluationCriterion(
                id = "principles",
                title = "Использованы принципы SOLID",
                grading = CriterionGrading.Range(
                    minValue = 0,
                    maxValue = 40,
                    multiplier = 0.4f,
                    maxPoints = 40,
                ),
            ),
            EvaluationCriterion(
                id = "tests",
                title = "Тесты написаны",
                grading = CriterionGrading.PassFail,
            ),
        )
    }
}
