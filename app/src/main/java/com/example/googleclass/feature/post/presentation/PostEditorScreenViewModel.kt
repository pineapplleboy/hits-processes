package com.example.googleclass.feature.post.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.course.domain.repository.CourseDetailRepository
import com.example.googleclass.feature.post.data.model.AttachmentDto
import com.example.googleclass.feature.post.data.model.PostCreateDto
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.post.data.model.PostUpdateDto
import com.example.googleclass.feature.post.data.model.TaskAnswerAppraisingType
import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.post.presentation.needsEvaluationFunction
import com.example.googleclass.feature.post.presentation.needsMaxScore
import com.example.googleclass.feature.post.presentation.needsMinScore
import com.example.googleclass.feature.post.presentation.needsMultiplier
import com.example.googleclass.feature.post.presentation.needsPassThreshold
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class PostEditorScreenViewModel(
    private val mode: PostEditorMode,
    private val postRepository: PostRepository,
    private val fileRepository: FileRepository,
    private val courseDetailRepository: CourseDetailRepository,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PostEditorScreenState> =
        MutableStateFlow(PostEditorScreenState.Loading)
    val uiState: StateFlow<PostEditorScreenState> = _uiState.asStateFlow()

    private val _uiEffect = MutableStateFlow<PostEditorUiEffect>(PostEditorUiEffect.None)
    val uiEffect: StateFlow<PostEditorUiEffect> = _uiEffect.asStateFlow()

    init {
        loadInitialData()
    }

    fun onEvent(event: PostEditorUiEvent) {
        when (event) {
            is PostEditorUiEvent.NavigateBack -> sendEffect(PostEditorUiEffect.NavigateBack)
            is PostEditorUiEvent.Save -> handleSave()
            is PostEditorUiEvent.TextChanged -> updateContent { copy(text = event.text) }
            is PostEditorUiEvent.PostTypeSelected -> handlePostTypeSelected(event.postType)
            is PostEditorUiEvent.TaskMarkEvaluationTypeSelected -> updateContent {
                copy(taskMarkEvaluationType = event.type)
            }
            is PostEditorUiEvent.MaxScoreChanged -> updateContent {
                copy(maxScore = event.value.filter { it.isDigit() || it == '.' })
            }
            is PostEditorUiEvent.MinScoreChanged -> updateContent {
                copy(minScore = event.value.filter { it.isDigit() || it == '.' })
            }
            is PostEditorUiEvent.MultiplierChanged -> updateContent {
                copy(multiplier = event.value.filter { it.isDigit() || it == '.' })
            }
            is PostEditorUiEvent.PassThresholdChanged -> updateContent {
                copy(passThreshold = event.value.filter { it.isDigit() || it == '.' })
            }
            is PostEditorUiEvent.EvaluationFunctionSelected -> updateContent {
                copy(evaluationFunction = event.function)
            }
            is PostEditorUiEvent.FileAttached -> handleFileAttached(event.uri, event.displayName)
            is PostEditorUiEvent.FileRemoved -> handleFileRemoved(event.uri)
            is PostEditorUiEvent.ExistingAttachmentRemoved -> handleExistingAttachmentRemoved(event.attachmentId)
            is PostEditorUiEvent.DeadlineChanged -> updateContent { copy(deadline = event.deadline) }
            is PostEditorUiEvent.PeerReviewToggled -> updateContent {
                val nextDeadline = if (event.enabled && appraiserDeadline.isBlank()) {
                    formatDeadlineForDisplay(System.currentTimeMillis())
                } else {
                    appraiserDeadline
                }
                copy(peerReviewEnabled = event.enabled, appraiserDeadline = nextDeadline)
            }
            is PostEditorUiEvent.StudentAppraisingNumberChanged -> updateContent {
                copy(studentAppraisingNumber = event.value.filter { it.isDigit() })
            }
            is PostEditorUiEvent.AppraiserDeadlineChanged -> updateContent {
                copy(appraiserDeadline = event.deadline)
            }
            is PostEditorUiEvent.TaskAnswerAppraisingTypeSelected -> updateContent {
                copy(taskAnswerAppraisingType = event.type)
            }
            is PostEditorUiEvent.CanSeeAppraiserToggled -> updateContent {
                copy(canSeeAppraiser = event.value)
            }
            is PostEditorUiEvent.CanSeeAppraisedToggled -> updateContent {
                copy(canSeeAppraised = event.value)
            }
        }
    }

    private fun loadInitialData() {
        val courseId = when (mode) {
            is PostEditorMode.Create -> mode.courseId
            is PostEditorMode.Edit -> mode.courseId
        }

        viewModelScope.launch {
            val courseResult = courseDetailRepository.getCourse(courseId)
            val courseEvalType = courseResult.getOrNull()?.courseMarkEvaluationType

            when (mode) {
                is PostEditorMode.Create -> {
                    val defaultDeadline = formatDeadlineForDisplay(System.currentTimeMillis())
                    _uiState.value = PostEditorScreenState.Content(
                        mode = mode,
                        text = "",
                        selectedPostType = PostType.ANNOUNCEMENT,
                        taskMarkEvaluationType = TaskMarkEvaluationType.TEACHER_DECISION,
                        courseMarkEvaluationType = courseEvalType,
                        maxScore = "",
                        minScore = "",
                        multiplier = "1",
                        passThreshold = "",
                        evaluationFunction = PostCreateDto.EvaluationFunction.SUM,
                        deadline = defaultDeadline,
                        attachedFiles = emptyList(),
                        existingAttachments = emptyList(),
                        isSaving = false,
                        isPostTypeEditable = true,
                        peerReviewEnabled = false,
                        studentAppraisingNumber = "",
                        appraiserDeadline = defaultDeadline,
                        taskAnswerAppraisingType = TaskAnswerAppraisingType.CHAIN,
                        canSeeAppraiser = false,
                        canSeeAppraised = false,
                    )
                }

                is PostEditorMode.Edit -> {
                    postRepository.getPost(mode.courseId, mode.postId)
                        .onSuccess { post ->
                            val deadlineDisplay = post.deadline?.takeIf { it.isNotBlank() }
                                ?.let { parseIsoToDisplay(it) }
                                ?: formatDeadlineForDisplay(System.currentTimeMillis())
                            _uiState.value = PostEditorScreenState.Content(
                                mode = mode,
                                text = post.text,
                                selectedPostType = post.postType,
                                taskMarkEvaluationType = post.taskMarkEvaluationType
                                    ?: TaskMarkEvaluationType.TEACHER_DECISION,
                                courseMarkEvaluationType = courseEvalType,
                                maxScore = if ((post.maxScore ?: 0f) > 0) post.maxScore?.roundToInt()?.toString() ?: "" else "",
                                minScore = post.minScore?.toString() ?: "",
                                multiplier = post.multiplier?.toString() ?: "1",
                                passThreshold = post.passThreshold?.toString() ?: "",
                                evaluationFunction = post.evaluationFunction
                                    ?: PostCreateDto.EvaluationFunction.SUM,
                                deadline = deadlineDisplay,
                                attachedFiles = emptyList(),
                                existingAttachments = post.files.map {
                                    ExistingAttachment(it.id, it.fileName?.takeIf { n -> n.isNotBlank() } ?: "Файл")
                                },
                                isSaving = false,
                                isPostTypeEditable = false,
                                peerReviewEnabled = post.taskAnswerAppraisingType != null,
                                studentAppraisingNumber = post.studentAppraisingNumber
                                    ?.takeIf { it > 0 }?.toString() ?: "",
                                appraiserDeadline = post.appraiserDeadline?.takeIf { it.isNotBlank() }
                                    ?.let { parseIsoToDisplay(it) }
                                    ?: formatDeadlineForDisplay(System.currentTimeMillis()),
                                taskAnswerAppraisingType = post.taskAnswerAppraisingType
                                    ?: TaskAnswerAppraisingType.CHAIN,
                                canSeeAppraiser = post.canSeeAppraiser ?: false,
                                canSeeAppraised = post.canSeeAppraised ?: false,
                            )
                        }
                        .onFailure {
                            sendEffect(PostEditorUiEffect.ShowError(
                                it.message ?: "Ошибка при загрузке публикации"
                            ))
                            sendEffect(PostEditorUiEffect.NavigateBack)
                        }
                }
            }
        }
    }

    private fun handlePostTypeSelected(postType: PostType) {
        updateContent {
            if (!isPostTypeEditable) return@updateContent this
            if (postType == PostType.TASK && deadline.isBlank()) {
                copy(
                    selectedPostType = postType,
                    deadline = formatDeadlineForDisplay(System.currentTimeMillis()),
                )
            } else {
                copy(selectedPostType = postType)
            }
        }
    }

    private fun handleFileAttached(uri: Uri, displayName: String) {
        updateContent {
            copy(attachedFiles = attachedFiles + PostAttachedFile(uri, displayName))
        }
    }

    private fun handleFileRemoved(uri: Uri) {
        updateContent {
            copy(attachedFiles = attachedFiles.filter { it.uri != uri })
        }
    }

    private fun handleExistingAttachmentRemoved(attachmentId: String) {
        updateContent {
            copy(existingAttachments = existingAttachments.filter { it.id != attachmentId })
        }
    }

    private fun handleSave() {
        val state = _uiState.value
        if (state !is PostEditorScreenState.Content || state.isSaving) return

        if (state.text.isBlank()) {
            sendEffect(PostEditorUiEffect.ShowError("Введите текст публикации"))
            return
        }

        if (state.selectedPostType == PostType.TASK && state.isPostTypeEditable) {
            if (state.deadline.isBlank()) {
                sendEffect(PostEditorUiEffect.ShowError("Укажите срок сдачи"))
                return
            }
            val deadlineMs = try {
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .parse(state.deadline.trim())?.time
            } catch (_: Exception) {
                null
            }
            if (deadlineMs != null && deadlineMs <= System.currentTimeMillis()) {
                sendEffect(PostEditorUiEffect.ShowError("Укажите дату и время в будущем"))
                return
            }
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val uploadedFileIds = mutableListOf<String>()

            for (file in state.attachedFiles) {
                val result = fileRepository.uploadFile(
                    uri = file.uri,
                    contentResolver = contentResolver,
                    onProgress = {},
                )
                if (result.isSuccess) {
                    uploadedFileIds.add(result.getOrThrow().id)
                } else {
                    _uiState.value = state.copy(isSaving = false)
                    sendEffect(PostEditorUiEffect.ShowError(
                        result.exceptionOrNull()?.message ?: "Ошибка при загрузке файла"
                    ))
                    return@launch
                }
            }

            val allFileIds = state.existingAttachments.map { it.id } + uploadedFileIds
            val files = allFileIds.map { AttachmentDto(id = it) }

            val courseId = when (mode) {
                is PostEditorMode.Create -> mode.courseId
                is PostEditorMode.Edit -> mode.courseId
            }

            val isTask = state.selectedPostType == PostType.TASK
            val peerReviewActive = isTask && state.peerReviewEnabled
            val appraiserDeadlineIso = if (peerReviewActive) {
                parseDisplayToIso(state.appraiserDeadline)
            } else {
                null
            }
            val appraisingType = if (peerReviewActive) state.taskAnswerAppraisingType else null
            // Количество работ актуально только для свободного выбора (ANY);
            // в режиме «по цепочке» (CHAIN) распределение задаёт сервер.
            val appraisingNumber = if (peerReviewActive &&
                appraisingType == TaskAnswerAppraisingType.ANY
            ) {
                state.studentAppraisingNumber.toIntOrNull()
            } else {
                null
            }
            val canSeeAppraiser = if (peerReviewActive) state.canSeeAppraiser else null
            val canSeeAppraised = if (peerReviewActive) state.canSeeAppraised else null

            val saveResult = when (mode) {
                is PostEditorMode.Create -> {
                    val deadlineIso = if (isTask) {
                        parseDisplayToIso(state.deadline) ?: formatDeadlineToIso(
                            System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                        )
                    } else {
                        null
                    }
                    val evalType = state.taskMarkEvaluationType
                    val courseEvalType = state.courseMarkEvaluationType
                    postRepository.createPost(
                        courseId = courseId,
                        post = PostCreateDto(
                            text = state.text,
                            files = files,
                            postType = state.selectedPostType,
                            taskMarkEvaluationType = if (isTask) evalType else null,
                            maxScore = if (isTask && evalType.needsMaxScore()) state.maxScore.toFloatOrNull() else null,
                            minScore = if (isTask && evalType.needsMinScore()) state.minScore.toFloatOrNull() else null,
                            multiplier = if (isTask && courseEvalType.needsMultiplier()) state.multiplier.toFloatOrNull() else null,
                            passThreshold = if (isTask && evalType.needsPassThreshold()) state.passThreshold.toFloatOrNull() else null,
                            evaluationFunction = if (isTask && courseEvalType.needsEvaluationFunction()) state.evaluationFunction else null,
                            deadline = deadlineIso,
                            appraiserDeadline = appraiserDeadlineIso,
                            studentAppraisingNumber = appraisingNumber,
                            taskAnswerAppraisingType = appraisingType,
                            canSeeAppraiser = canSeeAppraiser,
                            canSeeAppraised = canSeeAppraised,
                        ),
                    ).map { }
                }

                is PostEditorMode.Edit -> {
                    val editEvalType = state.taskMarkEvaluationType
                    val editCourseEvalType = state.courseMarkEvaluationType
                    postRepository.editPost(
                        courseId = courseId,
                        postId = mode.postId,
                        post = PostUpdateDto(
                            text = state.text,
                            files = files,
                            taskMarkEvaluationType = if (isTask) editEvalType else null,
                            maxScore = if (isTask && editEvalType.needsMaxScore()) state.maxScore.toFloatOrNull() else null,
                            minScore = if (isTask && editEvalType.needsMinScore()) state.minScore.toFloatOrNull() else null,
                            multiplier = if (isTask && editCourseEvalType.needsMultiplier()) state.multiplier.toFloatOrNull() else null,
                            passThreshold = if (isTask && editEvalType.needsPassThreshold()) state.passThreshold.toFloatOrNull() else null,
                            evaluationFunction = if (isTask && editCourseEvalType.needsEvaluationFunction()) state.evaluationFunction else null,
                            appraiserDeadline = appraiserDeadlineIso,
                            studentAppraisingNumber = appraisingNumber,
                            taskAnswerAppraisingType = appraisingType,
                            canSeeAppraiser = canSeeAppraiser,
                            canSeeAppraised = canSeeAppraised,
                        ),
                    )
                }
            }

            saveResult
                .onSuccess {
                    sendEffect(PostEditorUiEffect.NavigateToCourseFeed(courseId))
                }
                .onFailure {
                    _uiState.value = state.copy(isSaving = false)
                    sendEffect(PostEditorUiEffect.ShowError(
                        it.message ?: "Ошибка при сохранении публикации"
                    ))
                }
        }
    }

    private inline fun updateContent(
        transform: PostEditorScreenState.Content.() -> PostEditorScreenState.Content,
    ) {
        val state = _uiState.value
        if (state is PostEditorScreenState.Content) {
            _uiState.value = state.transform()
        }
    }

    private fun sendEffect(effect: PostEditorUiEffect) {
        _uiEffect.value = effect
    }

    fun consumeEffect() {
        _uiEffect.value = PostEditorUiEffect.None
    }

    companion object {
        private val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun formatDeadlineForDisplay(timestampMs: Long): String =
            displayFormat.format(Date(timestampMs))

        fun formatDeadlineToIso(timestampMs: Long): String =
            isoFormat.format(Date(timestampMs))

        fun parseDisplayToIso(display: String): String? = try {
            val date = displayFormat.parse(display.trim())
            if (date != null) isoFormat.format(date) else null
        } catch (_: Exception) {
            null
        }

        fun parseIsoToDisplay(iso: String): String? = try {
            val date = isoFormat.parse(iso.trim())
            if (date != null) displayFormat.format(date) else null
        } catch (_: Exception) {
            null
        }
    }
}
