package com.example.googleclass.feature.taskdetail.studentchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentChatViewModel(
    private val taskAnswerId: String,
    private val studentName: String,
    private val studentUserId: String,
    private val commentRepository: CommentRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<StudentChatUiState> =
        MutableStateFlow(StudentChatUiState.Loading)
    val uiState: StateFlow<StudentChatUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<StudentChatUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect

    init {
        loadComments()
    }

    fun onEvent(event: StudentChatUiEvent) {
        when (event) {
            is StudentChatUiEvent.NavigateBack -> sendEffect(StudentChatUiEffect.NavigateBack)
            is StudentChatUiEvent.SendMessage -> handleSendMessage()
            is StudentChatUiEvent.MessageInputChanged -> handleInputChanged(event.text)
        }
    }

    private fun handleInputChanged(text: String) {
        val state = _uiState.value
        if (state is StudentChatUiState.ChatContent) {
            _uiState.value = state.copy(messageInput = text)
        }
    }

    private fun handleSendMessage() {
        val state = _uiState.value
        if (state is StudentChatUiState.ChatContent && state.messageInput.isNotBlank()) {
            val text = state.messageInput
            _uiState.value = state.copy(messageInput = "")

            viewModelScope.launch {
                commentRepository.createTaskAnswerComment(taskAnswerId, text)
                    .onSuccess { loadComments() }
                    .onFailure {
                        sendEffect(StudentChatUiEffect.NavigateBack)
                    }
            }
        }
    }

    private fun sendEffect(effect: StudentChatUiEffect) {
        viewModelScope.launch {
            _uiEffect.tryEmit(effect)
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            commentRepository.getTaskAnswerCommentsAsChat(taskAnswerId, studentUserId)
                .onSuccess { messages ->
                    _uiState.value = StudentChatUiState.ChatContent(
                        studentId = taskAnswerId,
                        studentName = studentName,
                        messages = messages,
                        messageInput = (_uiState.value as? StudentChatUiState.ChatContent)?.messageInput.orEmpty(),
                    )
                }
                .onFailure {
                    _uiState.value = StudentChatUiState.ChatContent(
                        studentId = taskAnswerId,
                        studentName = studentName,
                        messages = emptyList(),
                        messageInput = "",
                    )
                }
        }
    }
}
