package com.example.googleclass.feature.post.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.post.data.model.AttachmentModel
import com.example.googleclass.feature.post.data.model.PostCreateModel
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.post.data.model.PostUpdateModel
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PostEditorViewModel(
    private val mode: PostEditorMode,
    private val postRepository: PostRepository,
    private val fileRepository: FileRepository,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PostEditorUiState> =
        MutableStateFlow(PostEditorUiState.Loading)
    val uiState: StateFlow<PostEditorUiState> = _uiState.asStateFlow()

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
            is PostEditorUiEvent.MaxScoreChanged -> handleMaxScoreChanged(event.value)
            is PostEditorUiEvent.FileAttached -> handleFileAttached(event.uri, event.displayName)
            is PostEditorUiEvent.FileRemoved -> handleFileRemoved(event.uri)
            is PostEditorUiEvent.ExistingAttachmentRemoved -> handleExistingAttachmentRemoved(event.attachmentId)
            is PostEditorUiEvent.DeadlineChanged -> updateContent { copy(deadline = event.deadline) }
        }
    }

    private fun loadInitialData() {
        when (mode) {
            is PostEditorMode.Create -> {
                val defaultDeadline = formatDeadlineForDisplay(System.currentTimeMillis())
                _uiState.value = PostEditorUiState.Content(
                    mode = mode,
                    text = "",
                    selectedPostType = PostType.ANNOUNCEMENT,
                    maxScore = "",
                    deadline = defaultDeadline,
                    attachedFiles = emptyList(),
                    existingAttachments = emptyList(),
                    isSaving = false,
                    isPostTypeEditable = true,
                )
            }

            is PostEditorMode.Edit -> {
                viewModelScope.launch {
                    postRepository.getPost(mode.courseId, mode.postId)
                        .onSuccess { post ->
                            val deadlineDisplay = post.deadline?.takeIf { it.isNotBlank() }
                                ?.let { parseIsoToDisplay(it) }
                                ?: formatDeadlineForDisplay(System.currentTimeMillis())
                            _uiState.value = PostEditorUiState.Content(
                                mode = mode,
                                text = post.text,
                                selectedPostType = post.postType,
                                maxScore = post.maxScore.toString(),
                                deadline = deadlineDisplay,
                                attachedFiles = emptyList(),
                                existingAttachments = post.files.map {
                                    ExistingAttachment(it.id, it.fileName?.takeIf { n -> n.isNotBlank() } ?: "Файл")
                                },
                                isSaving = false,
                                isPostTypeEditable = false,
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

    private fun handleMaxScoreChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        updateContent { copy(maxScore = filtered) }
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
        if (state !is PostEditorUiState.Content || state.isSaving) return

        if (state.text.isBlank()) {
            sendEffect(PostEditorUiEffect.ShowError("Введите текст публикации"))
            return
        }

        if (state.selectedPostType == PostType.TASK && state.isPostTypeEditable) {
            val score = state.maxScore.toIntOrNull()
            if (score == null || score <= 0) {
                sendEffect(PostEditorUiEffect.ShowError("Укажите максимальный балл"))
                return
            }
            if (state.deadline.isBlank()) {
                sendEffect(PostEditorUiEffect.ShowError("Укажите срок сдачи"))
                return
            }
            val deadlineMs = try {
                java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
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
            val files = allFileIds.map { AttachmentModel(id = it) }

            val courseId = when (mode) {
                is PostEditorMode.Create -> mode.courseId
                is PostEditorMode.Edit -> mode.courseId
            }
            val saveResult = when (mode) {
                is PostEditorMode.Create -> {
                    val deadlineIso = if (state.selectedPostType == PostType.TASK) {
                        parseDisplayToIso(state.deadline) ?: formatDeadlineToIso(
                            System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                        )
                    } else {
                        formatDeadlineToIso(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
                    }
                    postRepository.createPost(
                        courseId = courseId,
                        post = PostCreateModel(
                            text = state.text,
                            files = files,
                            postType = state.selectedPostType,
                            maxScore = state.maxScore.toIntOrNull() ?: 0,
                            deadline = deadlineIso,
                        ),
                    ).map { }
                }

                is PostEditorMode.Edit -> {
                    postRepository.editPost(
                        courseId = courseId,
                        postId = mode.postId,
                        post = PostUpdateModel(
                            text = state.text,
                            files = files,
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
        transform: PostEditorUiState.Content.() -> PostEditorUiState.Content,
    ) {
        val state = _uiState.value
        if (state is PostEditorUiState.Content) {
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
