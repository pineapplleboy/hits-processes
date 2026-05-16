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

class StudentChatScreenViewModel(
    private val taskAnswerId: String,
    private val studentName: String,
    private val studentUserId: String,
    private val currentUserId: String,
    private val commentRepository: CommentRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<StudentChatScreenState> =
        MutableStateFlow(StudentChatScreenState.Loading)
    val uiState: StateFlow<StudentChatScreenState> = _uiState.asStateFlow()

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
        if (state is StudentChatScreenState.ChatContent) {
            _uiState.value = state.copy(messageInput = text)
        }
    }

    private fun handleSendMessage() {
        val state = _uiState.value
        if (state is StudentChatScreenState.ChatContent && state.messageInput.isNotBlank()) {
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
            commentRepository.getTaskAnswerCommentsAsChat(taskAnswerId, currentUserId)
                .onSuccess { messages ->
                    _uiState.value = StudentChatScreenState.ChatContent(
                        studentId = taskAnswerId,
                        studentName = studentName,
                        currentUserId = currentUserId,
                        messages = messages,
                        messageInput = (_uiState.value as? StudentChatScreenState.ChatContent)?.messageInput.orEmpty(),
                    )
                }
                .onFailure {
                    _uiState.value = StudentChatScreenState.ChatContent(
                        studentId = taskAnswerId,
                        studentName = studentName,
                        currentUserId = currentUserId,
                        messages = emptyList(),
                        messageInput = "",
                    )
                }
        }
    }
}
