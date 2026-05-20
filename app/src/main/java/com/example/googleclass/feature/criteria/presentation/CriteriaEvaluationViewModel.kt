package com.example.googleclass.feature.criteria.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.criteria.domain.model.CriteriaScoreDraft
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.TaskAnswerCriteriaScore
import com.example.googleclass.feature.criteria.domain.usecase.GetMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetTaskAnswerCriteriaScoresUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutCriteriaScoreUseCase
import com.example.googleclass.feature.post.data.model.PostDto
import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType
import com.example.googleclass.feature.post.data.model.supportsCriteria
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.domain.repository.TaskAnswerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CriteriaEvaluationViewModel(
    private val courseId: String,
    private val postId: String,
    private val taskAnswerId: String,
    private val postRepository: PostRepository,
    private val taskAnswerRepository: TaskAnswerRepository,
    private val commentRepository: CommentRepository,
    private val getMarkCriteriaUseCase: GetMarkCriteriaUseCase,
    private val getTaskAnswerCriteriaScoresUseCase: GetTaskAnswerCriteriaScoresUseCase,
    private val putCriteriaScoreUseCase: PutCriteriaScoreUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CriteriaEvaluationUiState>(CriteriaEvaluationUiState.Loading)
    val uiState: StateFlow<CriteriaEvaluationUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CriteriaEvaluationUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        load()
    }

    fun onEvent(event: CriteriaEvaluationUiEvent) {
        when (event) {
            CriteriaEvaluationUiEvent.NavigateBack -> sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
            CriteriaEvaluationUiEvent.Save -> save()
            is CriteriaEvaluationUiEvent.ScoreChanged -> updateScore(event.markCriteriaId, event.value)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CriteriaEvaluationUiState.Loading
            val postDeferred = async { postRepository.getPost(courseId, postId) }
            val answersDeferred = async { taskAnswerRepository.getAllPostTaskAnswers(postId) }
            val criteriaDeferred = async { getMarkCriteriaUseCase(courseId, postId) }
            val scoresDeferred = async { getTaskAnswerCriteriaScoresUseCase(taskAnswerId) }
            val commentsDeferred = async { commentRepository.getTaskAnswerComments(taskAnswerId) }

            val postResult = postDeferred.await()
            val post = postResult.getOrNull()
            if (post == null) {
                sendEffect(
                    CriteriaEvaluationUiEffect.ShowMessage(
                        postResult.exceptionOrNull()?.message ?: "Не удалось загрузить задание",
                    ),
                )
                sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
                return@launch
            }

            if (!post.taskMarkEvaluationType.supportsCriteria()) {
                sendEffect(CriteriaEvaluationUiEffect.ShowMessage("Для этого типа задания критерии недоступны"))
                sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
                return@launch
            }

            val answersResult = answersDeferred.await()
            val answers = answersResult.getOrNull()
            if (answers == null) {
                sendEffect(
                    CriteriaEvaluationUiEffect.ShowMessage(
                        answersResult.exceptionOrNull()?.message
                            ?: "Не удалось загрузить работу студента",
                    ),
                )
                sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
                return@launch
            }

            val taskAnswer = answers.firstOrNull { it.id == taskAnswerId }
            if (taskAnswer == null) {
                sendEffect(CriteriaEvaluationUiEffect.ShowMessage("Работа студента не найдена"))
                sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
                return@launch
            }

            val criteria = criteriaDeferred.await().getOrDefault(emptyList())
            val existingScores = scoresDeferred.await().getOrDefault(emptyList())
            val comments = commentsDeferred.await().getOrDefault(emptyList())
            val fields = mergeCriteriaFields(criteria, existingScores)

            _uiState.value = CriteriaEvaluationUiState.Content(
                taskTitle = post.toTaskTitle(),
                studentName = taskAnswer.userName.orEmpty().ifBlank { "Студент" },
                taskMaxScore = post.maxScore,
                taskAnswerId = taskAnswerId,
                evaluationType = post.taskMarkEvaluationType,
                passThreshold = post.passThreshold,
                files = taskAnswer.files,
                comments = comments,
                criteria = fields,
                calculatedScore = calculateScore(
                    criteria = fields,
                    evaluationType = post.taskMarkEvaluationType,
                    taskMaxScore = post.maxScore,
                ),
                isSaving = false,
            )
        }
    }

    private fun updateScore(markCriteriaId: String, value: String) {
        val state = _uiState.value as? CriteriaEvaluationUiState.Content ?: return
        val updatedCriteria = state.criteria.map { criterion ->
            if (criterion.markCriteriaId == markCriteriaId) {
                criterion.copy(
                    input = value.filter { it.isDigit() || it == '.' || it == ',' || it == '-' },
                )
            } else {
                criterion
            }
        }

        _uiState.value = state.copy(
            criteria = updatedCriteria,
            calculatedScore = calculateScore(
                criteria = updatedCriteria,
                evaluationType = state.evaluationType,
                taskMaxScore = state.taskMaxScore,
            ),
        )
    }

    private fun save() {
        val state = _uiState.value as? CriteriaEvaluationUiState.Content ?: return
        if (state.isSaving) return
        if (state.criteria.isEmpty()) {
            sendEffect(CriteriaEvaluationUiEffect.ShowMessage("Для задания не настроены критерии"))
            return
        }

        val draftScores = state.criteria.map { criterion ->
            val parsedScore = criterion.input.toNormalizedFloatOrNull()
                ?: return sendEffect(
                    CriteriaEvaluationUiEffect.ShowMessage("Заполните оценки по всем критериям"),
                )

            if (parsedScore < criterion.minScore || parsedScore > criterion.maxScore) {
                return sendEffect(
                    CriteriaEvaluationUiEffect.ShowMessage(
                        "Оценка для \"${criterion.name}\" должна быть в диапазоне от ${formatScore(criterion.minScore)} до ${formatScore(criterion.maxScore)}",
                    ),
                )
            }

            CriteriaScoreDraft(
                markCriteriaId = criterion.markCriteriaId,
                score = parsedScore,
            )
        }

        if (state.calculatedScore == null) {
            return sendEffect(
                CriteriaEvaluationUiEffect.ShowMessage("Невозможно рассчитать итоговую оценку"),
            )
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            var failedSave: Result<Unit>? = null
            // Save criterion scores one by one to avoid hitting the backend with concurrent updates.
            for (draft in draftScores) {
                val result = putCriteriaScoreUseCase(taskAnswerId, draft)
                if (result.isFailure) {
                    failedSave = result
                    break
                }
            }

            if (failedSave != null) {
                _uiState.value = state.copy(isSaving = false)
                sendEffect(
                    CriteriaEvaluationUiEffect.ShowMessage(
                        failedSave.exceptionOrNull()?.message
                            ?: "Не удалось сохранить оценки по критериям",
                    ),
                )
                return@launch
            }

            _uiState.value = state.copy(isSaving = false)
            sendEffect(CriteriaEvaluationUiEffect.ShowMessage("Оценки по критериям сохранены"))
            sendEffect(CriteriaEvaluationUiEffect.NavigateBack)
        }
    }

    private fun mergeCriteriaFields(
        criteria: List<EvaluationCriterion>,
        existingScores: List<TaskAnswerCriteriaScore>,
    ): List<CriteriaEvaluationFieldState> {
        if (criteria.isEmpty() && existingScores.isEmpty()) return emptyList()

        val scoresById = existingScores.associateBy { it.markCriteriaId }

        return if (criteria.isNotEmpty()) {
            criteria.map { criterion ->
                CriteriaEvaluationFieldState(
                    markCriteriaId = criterion.id,
                    name = criterion.name,
                    minScore = criterion.minScore,
                    maxScore = criterion.maxScore,
                    multiplier = criterion.multiplier,
                    input = scoresById[criterion.id]?.score?.let(::formatScore).orEmpty(),
                )
            }
        } else {
            existingScores.map { criterion ->
                CriteriaEvaluationFieldState(
                    markCriteriaId = criterion.markCriteriaId,
                    name = criterion.name,
                    minScore = criterion.minScore,
                    maxScore = criterion.maxScore,
                    multiplier = criterion.multiplier,
                    input = criterion.score?.let(::formatScore).orEmpty(),
                )
            }
        }
    }

    private fun calculateScore(
        criteria: List<CriteriaEvaluationFieldState>,
        evaluationType: TaskMarkEvaluationType?,
        taskMaxScore: Float,
    ): Float? {
        if (criteria.isEmpty()) return null

        val parsedScores = criteria.map { criterion ->
            val score = criterion.input.toNormalizedFloatOrNull() ?: return null
            if (score < criterion.minScore || score > criterion.maxScore) return null
            score
        }

        val rawScore = when (evaluationType) {
            TaskMarkEvaluationType.MEAN_VALUE -> parsedScores.average().toFloat()
            TaskMarkEvaluationType.COEFFICIENTS_SUM -> criteria.indices.sumOf { index ->
                val multiplier = criteria[index].multiplier ?: 1f
                (parsedScores[index] * multiplier).toDouble()
            }.toFloat()

            TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE -> {
                val weightedScores = criteria.mapIndexed { index, criterion ->
                    parsedScores[index] * (criterion.multiplier ?: 1f)
                }
                val totalWeight = criteria.sumOf { (it.multiplier ?: 1f).toDouble() }.toFloat()
                if (totalWeight > 0f) {
                    weightedScores.sum() / totalWeight
                } else {
                    parsedScores.average().toFloat()
                }
            }

            else -> parsedScores.sum()
        }

        return rawScore.coerceIn(0f, taskMaxScore)
    }

    private fun sendEffect(effect: CriteriaEvaluationUiEffect) {
        _uiEffect.tryEmit(effect)
    }

    private fun String.toNormalizedFloatOrNull(): Float? = replace(',', '.').toFloatOrNull()

    private fun formatScore(value: Float): String =
        if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
}

private fun PostDto.toTaskTitle(): String = text.take(80)
