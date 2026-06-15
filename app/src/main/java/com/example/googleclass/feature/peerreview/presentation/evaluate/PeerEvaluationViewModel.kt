package com.example.googleclass.feature.peerreview.presentation.evaluate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.usecase.CriterionScoreInput
import com.example.googleclass.feature.criteria.domain.usecase.GetMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.calculateCriteriaScore
import com.example.googleclass.feature.peerreview.domain.usecase.GetPeerEvaluationDetailUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SubmitAppraiserCriteriaScoresUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SubmitAppraiserScoreUseCase
import com.example.googleclass.feature.post.data.model.supportsCriteria
import com.example.googleclass.feature.post.domain.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PeerEvaluationViewModel(
    private val courseId: String,
    private val postId: String,
    private val evaluationId: String,
    private val postRepository: PostRepository,
    private val getPeerEvaluationDetailUseCase: GetPeerEvaluationDetailUseCase,
    private val getMarkCriteriaUseCase: GetMarkCriteriaUseCase,
    private val submitAppraiserScoreUseCase: SubmitAppraiserScoreUseCase,
    private val submitAppraiserCriteriaScoresUseCase: SubmitAppraiserCriteriaScoresUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PeerEvaluationUiState>(PeerEvaluationUiState.Loading)
    val uiState: StateFlow<PeerEvaluationUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PeerEvaluationUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        load()
    }

    fun onEvent(event: PeerEvaluationUiEvent) {
        when (event) {
            PeerEvaluationUiEvent.NavigateBack -> _uiEffect.tryEmit(PeerEvaluationUiEffect.NavigateBack)
            PeerEvaluationUiEvent.Save -> save()
            is PeerEvaluationUiEvent.CriterionScoreChanged ->
                updateCriterionScore(event.markCriteriaId, event.value)
            is PeerEvaluationUiEvent.SingleScoreChanged -> updateSingleScore(event.value)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = PeerEvaluationUiState.Loading

            val detailDeferred = async { getPeerEvaluationDetailUseCase(evaluationId) }
            val postDeferred = async { postRepository.getPost(courseId, postId) }
            val criteriaDeferred = async { getMarkCriteriaUseCase(courseId, postId) }

            val detail = detailDeferred.await().getOrNull()
            val post = postDeferred.await().getOrNull()
            if (detail == null || post == null) {
                _uiState.value = PeerEvaluationUiState.Error("Не удалось загрузить работу для оценивания")
                return@launch
            }

            val criteria = criteriaDeferred.await().getOrDefault(emptyList())
            val usesCriteria = post.taskMarkEvaluationType.supportsCriteria() && criteria.isNotEmpty()
            val scoresById = detail.criteriaScores.associateBy { it.id }

            val fields = criteria.map { criterion ->
                PeerCriterionField(
                    markCriteriaId = criterion.id,
                    name = criterion.name,
                    description = criterion.description,
                    minScore = criterion.minScore,
                    maxScore = criterion.maxScore,
                    multiplier = criterion.multiplier,
                    input = scoresById[criterion.id]?.score?.let(::formatScore).orEmpty(),
                )
            }

            val taskMaxScore = post.maxScore ?: 0f

            _uiState.value = PeerEvaluationUiState.Content(
                studentName = detail.studentName.orEmpty().ifBlank { "Студент" },
                files = detail.files,
                usesCriteria = usesCriteria,
                evaluationType = post.taskMarkEvaluationType,
                taskMaxScore = taskMaxScore,
                criteria = fields,
                singleScoreInput = detail.score?.let(::formatScore).orEmpty(),
                calculatedScore = if (usesCriteria) {
                    calculate(fields, post.taskMarkEvaluationType, taskMaxScore)
                } else {
                    null
                },
                isSaving = false,
            )
        }
    }

    private fun updateCriterionScore(markCriteriaId: String, value: String) {
        val state = _uiState.value as? PeerEvaluationUiState.Content ?: return
        val sanitized = value.filter { it.isDigit() || it == '.' || it == ',' }
        val updated = state.criteria.map {
            if (it.markCriteriaId == markCriteriaId) it.copy(input = sanitized) else it
        }
        _uiState.value = state.copy(
            criteria = updated,
            calculatedScore = calculate(updated, state.evaluationType, state.taskMaxScore),
        )
    }

    private fun updateSingleScore(value: String) {
        val state = _uiState.value as? PeerEvaluationUiState.Content ?: return
        _uiState.value = state.copy(
            singleScoreInput = value.filter { it.isDigit() || it == '.' || it == ',' },
        )
    }

    private fun save() {
        val state = _uiState.value as? PeerEvaluationUiState.Content ?: return
        if (state.isSaving) return

        if (state.usesCriteria) {
            saveCriteria(state)
        } else {
            saveSingleScore(state)
        }
    }

    private fun saveCriteria(state: PeerEvaluationUiState.Content) {
        val drafts = state.criteria.map { criterion ->
            val parsed = criterion.input.replace(',', '.').toFloatOrNull()
                ?: return showMessage("Заполните оценки по всем критериям")
            if (parsed < criterion.minScore || parsed > criterion.maxScore) {
                return showMessage(
                    "Оценка для «${criterion.name}» должна быть от ${formatScore(criterion.minScore)} до ${formatScore(criterion.maxScore)}",
                )
            }
            CriteriaScoreDraft(markCriteriaId = criterion.markCriteriaId, score = parsed)
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            submitAppraiserCriteriaScoresUseCase(evaluationId, drafts)
                .onSuccess {
                    _uiEffect.tryEmit(PeerEvaluationUiEffect.ShowMessage("Оценка сохранена"))
                    _uiEffect.tryEmit(PeerEvaluationUiEffect.NavigateBack)
                }
                .onFailure {
                    _uiState.value = state.copy(isSaving = false)
                    _uiEffect.tryEmit(
                        PeerEvaluationUiEffect.ShowMessage(it.message ?: "Не удалось сохранить оценку"),
                    )
                }
        }
    }

    private fun saveSingleScore(state: PeerEvaluationUiState.Content) {
        val score = state.singleScoreInput.replace(',', '.').toFloatOrNull()
            ?: return showMessage("Введите оценку")
        if (score < 0f || score > state.taskMaxScore) {
            return showMessage("Оценка должна быть от 0 до ${formatScore(state.taskMaxScore)}")
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            submitAppraiserScoreUseCase(evaluationId, score)
                .onSuccess {
                    _uiEffect.tryEmit(PeerEvaluationUiEffect.ShowMessage("Оценка сохранена"))
                    _uiEffect.tryEmit(PeerEvaluationUiEffect.NavigateBack)
                }
                .onFailure {
                    _uiState.value = state.copy(isSaving = false)
                    _uiEffect.tryEmit(
                        PeerEvaluationUiEffect.ShowMessage(it.message ?: "Не удалось сохранить оценку"),
                    )
                }
        }
    }

    private fun calculate(
        fields: List<PeerCriterionField>,
        evaluationType: com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType?,
        taskMaxScore: Float,
    ): Float? = calculateCriteriaScore(
        inputs = fields.map {
            CriterionScoreInput(
                rawInput = it.input,
                minScore = it.minScore,
                maxScore = it.maxScore,
                multiplier = it.multiplier,
            )
        },
        evaluationType = evaluationType,
        taskMaxScore = taskMaxScore,
    )

    private fun showMessage(message: String) {
        _uiEffect.tryEmit(PeerEvaluationUiEffect.ShowMessage(message))
    }

    private fun formatScore(value: Float): String =
        if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
}
