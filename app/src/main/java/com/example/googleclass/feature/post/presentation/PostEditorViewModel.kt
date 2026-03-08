package com.example.googleclass.feature.post.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.post.data.model.PostCreateModel
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.post.data.model.PostUpdateModel
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostEditorViewModel(
    private val mode: PostEditorMode,
    private val postRepository: PostRepository,
    private val fileRepository: FileRepository,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val _uiState: MutableStateFlow<PostEditorUiState> =
        MutableStateFlow(PostEditorUiState.Loading)
    val uiState: StateFlow<PostEditorUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PostEditorUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect

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
        }
    }

    private fun loadInitialData() {
        when (mode) {
            is PostEditorMode.Create -> {
                _uiState.value = PostEditorUiState.Content(
                    mode = mode,
                    text = "",
                    selectedPostType = PostType.ANNOUNCEMENT,
                    maxScore = "",
                    attachedFiles = emptyList(),
                    existingAttachmentIds = emptyList(),
                    isSaving = false,
                    isPostTypeEditable = true,
                )
            }

            is PostEditorMode.Edit -> {
                viewModelScope.launch {
                    postRepository.getPost(mode.courseId, mode.postId)
                        .onSuccess { post ->
                            _uiState.value = PostEditorUiState.Content(
                                mode = mode,
                                text = post.text,
                                selectedPostType = post.postType,
                                maxScore = post.maxScore.toString(),
                                attachedFiles = emptyList(),
                                existingAttachmentIds = post.attachments.map { it.id },
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
            if (isPostTypeEditable) copy(selectedPostType = postType) else this
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
            copy(existingAttachmentIds = existingAttachmentIds.filter { it != attachmentId })
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

            val allFileIds = state.existingAttachmentIds + uploadedFileIds

            val saveResult = when (mode) {
                is PostEditorMode.Create -> {
                    postRepository.createPost(
                        courseId = mode.courseId,
                        post = PostCreateModel(
                            text = state.text,
                            files = allFileIds,
                            postType = state.selectedPostType,
                            maxScore = state.maxScore.toIntOrNull() ?: 0,
                        ),
                    ).map { }
                }

                is PostEditorMode.Edit -> {
                    postRepository.editPost(
                        courseId = mode.courseId,
                        postId = mode.postId,
                        post = PostUpdateModel(
                            text = state.text,
                            files = allFileIds,
                        ),
                    )
                }
            }

            saveResult
                .onSuccess {
                    sendEffect(PostEditorUiEffect.PostSaved)
                    sendEffect(PostEditorUiEffect.NavigateBack)
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
        viewModelScope.launch {
            _uiEffect.tryEmit(effect)
        }
    }
}
