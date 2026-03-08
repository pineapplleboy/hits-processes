package com.example.googleclass.feature.taskdetail.studentchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentChatViewModel(
    private val studentId: String,
    private val studentName: String,
) : ViewModel() {

    private val _uiState: MutableStateFlow<StudentChatUiState> =
        MutableStateFlow(StudentChatUiState.Loading)
    val uiState: StateFlow<StudentChatUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<StudentChatUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect

    init {
        loadMockData()
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
            val newMessage = ChatMessage(
                id = (state.messages.size + 1).toString(),
                text = state.messageInput,
                authorName = "Преподаватель",
                createdAt = "Сейчас",
                isFromTeacher = true,
            )
            _uiState.value = state.copy(
                messages = state.messages + newMessage,
                messageInput = "",
            )
        }
    }

    private fun sendEffect(effect: StudentChatUiEffect) {
        viewModelScope.launch {
            _uiEffect.tryEmit(effect)
        }
    }

    private fun loadMockData() {
        _uiState.value = StudentChatUiState.ChatContent(
            studentId = studentId,
            studentName = studentName,
            messages = listOf(
                ChatMessage(
                    id = "1",
                    text = "Здравствуйте! Хотел уточнить по заданию — нужно ли реализовывать сортировку пузырьком или можно использовать встроенные функции?",
                    authorName = studentName,
                    createdAt = "10:15",
                    isFromTeacher = false,
                ),
                ChatMessage(
                    id = "2",
                    text = "Здравствуйте! Можно использовать любой метод сортировки, главное — чтобы алгоритм был корректным.",
                    authorName = "Преподаватель",
                    createdAt = "10:22",
                    isFromTeacher = true,
                ),
                ChatMessage(
                    id = "3",
                    text = "Понял, спасибо! Тогда я использую встроенную сортировку.",
                    authorName = studentName,
                    createdAt = "10:25",
                    isFromTeacher = false,
                ),
                ChatMessage(
                    id = "4",
                    text = "Отлично. Не забудьте также реализовать функцию поиска элемента и удаления дубликатов.",
                    authorName = "Преподаватель",
                    createdAt = "10:28",
                    isFromTeacher = true,
                ),
            ),
            messageInput = "",
        )
    }
}
