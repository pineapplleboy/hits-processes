package com.example.googleclass.feature.criteria.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.EvaluationFunction
import com.example.googleclass.feature.criteria.domain.model.MarkCriteriaDraft
import com.example.googleclass.feature.criteria.domain.model.usesPassFailScale
import com.example.googleclass.feature.criteria.domain.usecase.CreateMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.DeleteMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetCriteriaSettingsUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.UpdateMarkCriteriaUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CriteriaViewModel(
    private val courseId: String,
    private val postId: String,
    private val getMarkCriteriaUseCase: GetMarkCriteriaUseCase,
    private val getCriteriaSettingsUseCase: GetCriteriaSettingsUseCase,
    private val createMarkCriteriaUseCase: CreateMarkCriteriaUseCase,
    private val updateMarkCriteriaUseCase: UpdateMarkCriteriaUseCase,
    private val deleteMarkCriteriaUseCase: DeleteMarkCriteriaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CriteriaUiState>(CriteriaUiState.Loading)
    val uiState: StateFlow<CriteriaUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CriteriaUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadCriteria(showLoading = true)
    }

    fun onEvent(event: CriteriaUiEvent) {
        when (event) {
            CriteriaUiEvent.NavigateBack -> sendEffect(CriteriaUiEffect.NavigateBack)
            CriteriaUiEvent.Retry -> loadCriteria(showLoading = true)
            CriteriaUiEvent.AddCriterion -> openCreateEditor()
            CriteriaUiEvent.DismissEditor -> updateContent { copy(editor = null) }
            CriteriaUiEvent.SaveCriterion -> saveCriterion()
            CriteriaUiEvent.DismissDelete -> updateContent { copy(pendingDelete = null) }
            CriteriaUiEvent.ConfirmDelete -> confirmDelete()
            is CriteriaUiEvent.EditCriterion -> openEditEditor(event.criterionId)
            is CriteriaUiEvent.RequestDeleteCriterion -> requestDelete(event.criterionId)
            is CriteriaUiEvent.NameChanged -> updateEditor { copy(name = event.value) }
            is CriteriaUiEvent.PassFailChanged -> updateEditor {
                copy(
                    isPassFail = event.enabled,
                    minScore = if (event.enabled) "0" else minScore,
                    maxScore = if (event.enabled) "1" else maxScore,
                )
            }

            is CriteriaUiEvent.MinScoreChanged -> updateEditor {
                copy(minScore = sanitizeDecimalInput(event.value))
            }

            is CriteriaUiEvent.MaxScoreChanged -> updateEditor {
                copy(maxScore = sanitizeDecimalInput(event.value))
            }

            is CriteriaUiEvent.MultiplierChanged -> updateEditor {
                copy(multiplier = sanitizeDecimalInput(event.value))
            }
        }
    }

    private fun loadCriteria(showLoading: Boolean) {
        val currentState = _uiState.value as? CriteriaUiState.Content
        if (showLoading || currentState == null) {
            _uiState.value = CriteriaUiState.Loading
        } else {
            _uiState.value = currentState.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            val settingsResult = getCriteriaSettingsUseCase(courseId, postId)
            val settings = settingsResult.getOrElse {
                sendEffect(
                    CriteriaUiEffect.ShowMessage(
                        it.message ?: "Не удалось загрузить настройки критериев",
                    ),
                )
                null
            }

            if (settings != null && !settings.criteriaEnabled) {
                sendEffect(CriteriaUiEffect.ShowMessage("Для этого типа задания критерии недоступны"))
                sendEffect(CriteriaUiEffect.NavigateBack)
                return@launch
            }

            val criteriaResult = getMarkCriteriaUseCase(courseId, postId)
            val allowsMultiplier = settings?.allowsMultiplier ?: currentState?.allowsMultiplier ?: false

            criteriaResult
                .onSuccess { criteria ->
                    _uiState.value = CriteriaUiState.Content(
                        criteria = criteria,
                        allowsMultiplier = allowsMultiplier,
                        isRefreshing = false,
                    )
                }
                .onFailure { error ->
                    _uiState.value = CriteriaUiState.Content(
                        criteria = currentState?.criteria.orEmpty(),
                        allowsMultiplier = allowsMultiplier,
                        isRefreshing = false,
                        editor = currentState?.editor?.copy(allowsMultiplier = allowsMultiplier),
                        pendingDelete = currentState?.pendingDelete,
                    )
                    sendEffect(
                        CriteriaUiEffect.ShowMessage(
                            error.message ?: "Не удалось загрузить критерии",
                        ),
                    )
                }
        }
    }

    private fun openCreateEditor() {
        updateContent {
            copy(
                editor = CriterionEditorState(
                    mode = CriterionEditorMode.CREATE,
                    minScore = "0",
                    allowsMultiplier = allowsMultiplier,
                ),
                pendingDelete = null,
            )
        }
    }

    private fun openEditEditor(criterionId: String) {
        val state = _uiState.value as? CriteriaUiState.Content ?: return
        val criterion = state.criteria.firstOrNull { it.id == criterionId } ?: return
        _uiState.value = state.copy(
            editor = criterion.toEditorState(state.allowsMultiplier),
            pendingDelete = null,
        )
    }

    private fun requestDelete(criterionId: String) {
        updateContent {
            copy(
                pendingDelete = criteria.firstOrNull { it.id == criterionId },
                editor = null,
            )
        }
    }

    private fun confirmDelete() {
        val state = _uiState.value as? CriteriaUiState.Content ?: return
        val criterion = state.pendingDelete ?: return

        updateContent { copy(pendingDelete = null) }

        viewModelScope.launch {
            deleteMarkCriteriaUseCase(courseId, postId, criterion.id)
                .onSuccess {
                    updateContent {
                        copy(criteria = criteria.filterNot { it.id == criterion.id })
                    }
                    sendEffect(CriteriaUiEffect.ShowMessage("Критерий удалён"))
                }
                .onFailure {
                    sendEffect(
                        CriteriaUiEffect.ShowMessage(
                            it.message ?: "Не удалось удалить критерий",
                        ),
                    )
                }
        }
    }

    private fun saveCriterion() {
        val state = _uiState.value as? CriteriaUiState.Content ?: return
        val editor = state.editor ?: return
        if (editor.isSaving) return

        val draft = editor.toDraftOrError()
            ?: return sendEffect(CriteriaUiEffect.ShowMessage(validationMessage(editor)))

        updateEditor { copy(isSaving = true) }

        viewModelScope.launch {
            when (editor.mode) {
                CriterionEditorMode.CREATE -> {
                    createMarkCriteriaUseCase(courseId, postId, draft)
                        .onSuccess { createdId ->
                            val createdCriterion = EvaluationCriterion(
                                id = createdId,
                                name = draft.name,
                                evaluationFunction = state.criteria.firstOrNull()?.evaluationFunction
                                    ?: EvaluationFunction.SUM,
                                multiplier = draft.multiplier,
                                minScore = draft.minScore,
                                maxScore = draft.maxScore,
                                postId = postId,
                            )
                            updateContent {
                                copy(
                                    criteria = criteria + createdCriterion,
                                    editor = null,
                                )
                            }
                            sendEffect(CriteriaUiEffect.ShowMessage("Критерий добавлен"))
                        }
                        .onFailure {
                            updateEditor { copy(isSaving = false) }
                            sendEffect(
                                CriteriaUiEffect.ShowMessage(
                                    it.message ?: "Не удалось создать критерий",
                                ),
                            )
                        }
                }

                CriterionEditorMode.EDIT -> {
                    val criterionId = editor.criterionId ?: return@launch
                    updateMarkCriteriaUseCase(courseId, postId, criterionId, draft)
                        .onSuccess {
                            updateContent {
                                copy(
                                    criteria = criteria.map { criterion ->
                                        if (criterion.id == criterionId) {
                                            criterion.copy(
                                                name = draft.name,
                                                minScore = draft.minScore,
                                                maxScore = draft.maxScore,
                                                multiplier = draft.multiplier,
                                            )
                                        } else {
                                            criterion
                                        }
                                    },
                                    editor = null,
                                )
                            }
                            sendEffect(CriteriaUiEffect.ShowMessage("Критерий обновлён"))
                        }
                        .onFailure {
                            updateEditor { copy(isSaving = false) }
                            sendEffect(
                                CriteriaUiEffect.ShowMessage(
                                    it.message ?: "Не удалось обновить критерий",
                                ),
                            )
                        }
                }
            }
        }
    }

    private fun updateEditor(
        transform: CriterionEditorState.() -> CriterionEditorState,
    ) {
        updateContent {
            copy(editor = editor?.transform())
        }
    }

    private inline fun updateContent(
        transform: CriteriaUiState.Content.() -> CriteriaUiState.Content,
    ) {
        val state = _uiState.value
        if (state is CriteriaUiState.Content) {
            _uiState.value = state.transform()
        }
    }

    private fun CriterionEditorState.toDraftOrError(): MarkCriteriaDraft? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return null

        val min = if (isPassFail) 0f else minScore.toNormalizedFloatOrNull() ?: return null
        val max = if (isPassFail) 1f else maxScore.toNormalizedFloatOrNull() ?: return null
        if (max <= min) return null

        val parsedMultiplier = if (allowsMultiplier) {
            multiplier
                .takeIf { it.isNotBlank() }
                ?.toNormalizedFloatOrNull()
                ?: if (multiplier.isBlank()) null else return null
        } else {
            null
        }

        return MarkCriteriaDraft(
            name = trimmedName,
            minScore = min,
            maxScore = max,
            multiplier = parsedMultiplier,
        )
    }

    private fun validationMessage(editor: CriterionEditorState): String = when {
        editor.name.isBlank() -> "Введите название критерия"
        !editor.isPassFail && editor.minScore.toNormalizedFloatOrNull() == null ->
            "Введите корректный минимальный балл"

        !editor.isPassFail && editor.maxScore.toNormalizedFloatOrNull() == null ->
            "Введите корректный максимальный балл"

        !editor.isPassFail &&
            (editor.maxScore.toNormalizedFloatOrNull() ?: 0f) <=
            (editor.minScore.toNormalizedFloatOrNull() ?: 0f) ->
            "Максимальный балл должен быть больше минимального"

        editor.allowsMultiplier &&
            editor.multiplier.isNotBlank() &&
            editor.multiplier.toNormalizedFloatOrNull() == null ->
            "Введите корректный коэффициент"

        else -> "Проверьте параметры критерия"
    }

    private fun EvaluationCriterion.toEditorState(allowsMultiplier: Boolean): CriterionEditorState =
        CriterionEditorState(
            mode = CriterionEditorMode.EDIT,
            criterionId = id,
            name = name,
            isPassFail = usesPassFailScale,
            minScore = formatDecimal(minScore),
            maxScore = formatDecimal(maxScore),
            allowsMultiplier = allowsMultiplier,
            multiplier = if (allowsMultiplier) multiplier?.let(::formatDecimal).orEmpty() else "",
        )

    private fun sanitizeDecimalInput(value: String): String =
        value.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }

    private fun String.toNormalizedFloatOrNull(): Float? = replace(',', '.').toFloatOrNull()

    private fun formatDecimal(value: Float): String =
        if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

    private fun sendEffect(effect: CriteriaUiEffect) {
        _uiEffect.tryEmit(effect)
    }
}
