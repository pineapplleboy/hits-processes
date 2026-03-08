package com.example.googleclass.feature.post.presentation

import android.net.Uri
import com.example.googleclass.feature.post.data.model.PostType

// -- Mode --

sealed interface PostEditorMode {
    data class Create(val courseId: String) : PostEditorMode
    data class Edit(val courseId: String, val postId: String) : PostEditorMode
}

// -- State --

sealed interface PostEditorUiState {

    data object Loading : PostEditorUiState

    data class Content(
        val mode: PostEditorMode,
        val text: String,
        val selectedPostType: PostType,
        val maxScore: String,
        val attachedFiles: List<PostAttachedFile>,
        val existingAttachmentIds: List<String>,
        val isSaving: Boolean,
        val isPostTypeEditable: Boolean,
    ) : PostEditorUiState
}

data class PostAttachedFile(
    val uri: Uri,
    val displayName: String,
)

// -- Events --

sealed interface PostEditorUiEvent {
    data object NavigateBack : PostEditorUiEvent
    data object Save : PostEditorUiEvent
    data class TextChanged(val text: String) : PostEditorUiEvent
    data class PostTypeSelected(val postType: PostType) : PostEditorUiEvent
    data class MaxScoreChanged(val value: String) : PostEditorUiEvent
    data class FileAttached(val uri: Uri, val displayName: String) : PostEditorUiEvent
    data class FileRemoved(val uri: Uri) : PostEditorUiEvent
    data class ExistingAttachmentRemoved(val attachmentId: String) : PostEditorUiEvent
}

// -- Effects --

sealed interface PostEditorUiEffect {
    data object NavigateBack : PostEditorUiEffect
    data class ShowError(val message: String) : PostEditorUiEffect
    data object PostSaved : PostEditorUiEffect
}
