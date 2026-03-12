package com.example.googleclass.feature.taskdetail.studentchat.presentation

import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage

sealed interface StudentChatUiState {
    data object Loading : StudentChatUiState

    data class ChatContent(
        val studentId: String,
        val studentName: String,
        val currentUserId: String,
        val messages: List<ChatMessage>,
        val messageInput: String,
    ) : StudentChatUiState
}

sealed interface StudentChatUiEvent {
    data object NavigateBack : StudentChatUiEvent
    data object SendMessage : StudentChatUiEvent
    data class MessageInputChanged(val text: String) : StudentChatUiEvent
}

sealed interface StudentChatUiEffect {
    data object None : StudentChatUiEffect
    data object NavigateBack : StudentChatUiEffect
}
