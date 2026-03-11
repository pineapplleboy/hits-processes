package com.example.googleclass.feature.taskdetail.presentation

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.service.FileTransferService
import org.koin.androidx.compose.koinViewModel

@Composable
fun TaskDetailScreen(
    userRole: UserRole,
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToStudentChat: (studentId: String, studentName: String) -> Unit = { _, _ -> },
) {
    val viewModel: TaskDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val activity = context as ComponentActivity
    val filePickerDelegate = remember {
        FilePickerDelegate(
            registry = activity.activityResultRegistry,
            contentResolver = activity.contentResolver,
            context = activity,
        )
    }

    DisposableEffect(filePickerDelegate) {
        onDispose {
            filePickerDelegate.unregister()
        }
    }

    filePickerDelegate.onFilePicked = { uri, displayName ->
        viewModel.onEvent(TaskDetailUiEvent.FileAttached(uri, displayName))
    }

    LaunchedEffect(userRole, taskId) {
        when (userRole) {
            UserRole.STUDENT -> viewModel.loadStudentMockData()
            UserRole.TEACHER, UserRole.MAIN_TEACHER -> viewModel.loadTeacherMockData()
        }

        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TaskDetailUiEffect.NavigateBack -> onNavigateBack()

                is TaskDetailUiEffect.NavigateToStudentChat -> {
                    onNavigateToStudentChat(effect.studentId, effect.studentName)
                }

                is TaskDetailUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is TaskDetailUiEffect.StartFileUpload -> {
                    val intent = Intent(context, FileTransferService::class.java).apply {
                        action = FileTransferService.ACTION_UPLOAD
                        putParcelableArrayListExtra(
                            FileTransferService.EXTRA_FILE_URIS,
                            ArrayList(effect.uris),
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ContextCompat.startForegroundService(context, intent)
                    showToast(context, R.string.upload_started)
                }

                is TaskDetailUiEffect.StartFileDownload -> {
                    val intent = Intent(context, FileTransferService::class.java).apply {
                        action = FileTransferService.ACTION_DOWNLOAD
                        putExtra(FileTransferService.EXTRA_FILE_ID, effect.fileId)
                    }
                    ContextCompat.startForegroundService(context, intent)
                    showToast(context, R.string.download_started)
                }

                is TaskDetailUiEffect.None -> {}
            }
        }
    }

    TaskDetailContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onPickFromDocuments = { filePickerDelegate.launchDocuments() },
        onPickFromGallery = { filePickerDelegate.launchGallery() },
    )
}

fun showToast(context: Context, @StringRes text: Int) {
    Toast.makeText(
        context,
        context.getString(text),
        Toast.LENGTH_SHORT,
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailContent(
    state: TaskDetailUiState,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit = {},
    onPickFromGallery: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(TaskDetailUiEvent.NavigateBack) }) {
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
    ) { padding ->
        when (state) {
            is TaskDetailUiState.Loading -> LoadingState()

            is TaskDetailUiState.StudentView -> StudentViewContent(
                state = state,
                onEvent = onEvent,
                onPickFromDocuments = onPickFromDocuments,
                onPickFromGallery = onPickFromGallery,
                modifier = Modifier.padding(padding),
            )

            is TaskDetailUiState.TeacherView -> TeacherViewContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun StudentViewContent(
    state: TaskDetailUiState.StudentView,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TaskInfoCard(task = state.task)

        if (state.submission != null) {
            SubmissionCard(submission = state.submission)
        } else {
            SubmitWorkCard(
                attachedFiles = state.attachedFiles,
                onPickFromDocuments = onPickFromDocuments,
                onPickFromGallery = onPickFromGallery,
                onEvent = onEvent,
            )
        }

        StudentCommentsSection(
            selectedTab = state.selectedTab,
            publicComments = state.publicComments,
            privateComments = state.privateComments,
            commentInput = state.commentInput,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TeacherViewContent(
    state: TaskDetailUiState.TeacherView,
    onEvent: (TaskDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TaskInfoCard(task = state.task)

        TeacherCommentsSection(
            selectedTab = state.selectedTab,
            publicComments = state.publicComments,
            students = state.students,
            commentInput = state.commentInput,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentSubmittedPreview() {
    GoogleClassTheme {
        TaskDetailContent(
            state = TaskDetailUiState.StudentView(
                task = TaskDetail(
                    id = "1",
                    title = "Задание 1: Основы синтаксиса",
                    authorName = "Иванов Иван Иванович",
                    createdAt = "17 января, 14:00",
                    description = "Напишите программу, которая выводит \"Hello, World!\" и вычисляет сумму чисел от 1 до 100.",
                    deadline = "20 февраля, 23:59",
                    maxScore = 100,
                ),
                submission = Submission(
                    submittedAt = "18 февраля, 15:30",
                    files = listOf("solution1.py"),
                    score = 95,
                    maxScore = 100,
                    isNewGrade = true,
                ),
                publicComments = emptyList(),
                privateComments = listOf(
                    Comment("1", "Иванов Иван Иванович", "Отличная работа!", "19 февраля, 10:00"),
                    Comment("2", "Сидоров Алексей", "Спасибо!", "19 февраля, 11:00"),
                ),
                attachedFiles = emptyList(),
                commentInput = "",
                selectedTab = StudentTab.PUBLIC_COMMENTS,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentNotSubmittedPreview() {
    GoogleClassTheme {
        TaskDetailContent(
            state = TaskDetailUiState.StudentView(
                task = TaskDetail(
                    id = "2",
                    title = "Задание 2: Работа со списками",
                    authorName = "Петрова Мария Сергеевна",
                    createdAt = "1 февраля, 10:00",
                    description = "Реализуйте функции для работы со списками: сортировка, поиск элемента, удаление дубликатов.",
                    deadline = "25 февраля, 23:59",
                    maxScore = 100,
                ),
                submission = null,
                publicComments = emptyList(),
                privateComments = emptyList(),
                attachedFiles = emptyList(),
                commentInput = "",
                selectedTab = StudentTab.PUBLIC_COMMENTS,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeacherViewPreview() {
    GoogleClassTheme {
        TaskDetailContent(
            state = TaskDetailUiState.TeacherView(
                task = TaskDetail(
                    id = "2",
                    title = "Задание 2: Работа со списками",
                    authorName = "Петрова Мария Сергеевна",
                    createdAt = "1 февраля, 10:00",
                    description = "Реализуйте функции для работы со списками: сортировка, поиск элемента, удаление дубликатов.",
                    deadline = "25 февраля, 23:59",
                    maxScore = 100,
                ),
                publicComments = emptyList(),
                students = listOf(
                    StudentSubmissionInfo("1", "Сидоров Алексей", null, 100, SubmissionStatus.OVERDUE),
                    StudentSubmissionInfo("2", "Козлова Анна", null, 100, SubmissionStatus.OVERDUE),
                    StudentSubmissionInfo("3", "Смирнов Дмитрий", null, 100, SubmissionStatus.OVERDUE),
                ),
                commentInput = "",
                selectedTab = TeacherTab.STUDENTS,
            ),
            onEvent = {},
        )
    }
}
