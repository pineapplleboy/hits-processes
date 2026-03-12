package com.example.googleclass.feature.taskdetail.studentchat.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.ChatInputBar
import com.example.googleclass.common.presentation.component.ChatMessageList
import com.example.googleclass.common.presentation.component.ChatMessageUiModel
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.feature.taskdetail.studentchat.domain.model.ChatMessage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun StudentChatScreen(
    taskAnswerId: String,
    studentName: String,
    studentUserId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: StudentChatViewModel = koinViewModel(
        parameters = { parametersOf(taskAnswerId, studentName, studentUserId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is StudentChatUiEffect.NavigateBack -> onNavigateBack()
                is StudentChatUiEffect.None -> {}
            }
        }
    }

    StudentChatContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentChatContent(
    state: StudentChatUiState,
    onEvent: (StudentChatUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val title = if (state is StudentChatUiState.ChatContent) state.studentName else ""

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (state is StudentChatUiState.ChatContent) {
                ChatInputBar(
                    value = state.messageInput,
                    onValueChange = { onEvent(StudentChatUiEvent.MessageInputChanged(it)) },
                    onSend = { onEvent(StudentChatUiEvent.SendMessage) },
                )
            }
        },
    ) { padding ->
        when (state) {
            is StudentChatUiState.Loading -> LoadingState()
            is StudentChatUiState.ChatContent -> ChatMessageList(
                messages = state.messages.map {
                    it.toUiModel(
                        studentName = state.studentName,
                        youLabel = stringResource(R.string.chat_you),
                    )
                },
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun ChatMessage.toUiModel(studentName: String, youLabel: String = "Вы") = ChatMessageUiModel(
    id = id,
    text = text,
    authorName = if (isFromTeacher) youLabel else studentName,
    createdAt = createdAt,
    isOutgoing = isFromTeacher,
)
