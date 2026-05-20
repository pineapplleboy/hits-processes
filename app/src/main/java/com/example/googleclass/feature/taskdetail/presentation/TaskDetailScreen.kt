package com.example.googleclass.feature.taskdetail.presentation

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.service.FileTransferService
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TaskDetailScreen(
    courseId: String,
    postId: String,
    userRole: UserRole,
    refreshSignal: Boolean = false,
    onRefreshSignalConsumed: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (courseId: String, postId: String) -> Unit = { _, _ -> },
    onNavigateToCriteria: (courseId: String, postId: String) -> Unit = { _, _ -> },
    onNavigateToCourseFeed: (courseId: String) -> Unit = {},
    onNavigateToStudentChat: (taskAnswerId: String, studentName: String, studentUserId: String, currentUserId: String) -> Unit = { _, _, _, _ -> },
    onNavigateToCriteriaEvaluation: (courseId: String, postId: String, taskAnswerId: String) -> Unit = { _, _, _ -> },
) {
    val viewModel: TaskDetailScreenViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId, userRole) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasSeenFirstResume by remember { mutableStateOf(false) }

    val filePicker = rememberFilePicker(
        onFilePicked = { uri, displayName ->
            viewModel.onEvent(TaskDetailUiEvent.FileAttached(uri, displayName))
        },
    )

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TaskDetailUiEffect.NavigateBack -> onNavigateBack()

                is TaskDetailUiEffect.NavigateToEdit -> {
                    onNavigateToEdit(effect.courseId, effect.postId)
                }

                is TaskDetailUiEffect.NavigateToCriteria -> {
                    onNavigateToCriteria(effect.courseId, effect.postId)
                }

                is TaskDetailUiEffect.NavigateToCourseFeed -> {
                    Toast.makeText(context, "Публикация удалена", Toast.LENGTH_SHORT).show()
                    onNavigateToCourseFeed(effect.courseId)
                }

                is TaskDetailUiEffect.NavigateToStudentChat -> {
                    onNavigateToStudentChat(
                        effect.taskAnswerId,
                        effect.studentName,
                        effect.studentUserId,
                        effect.currentUserId,
                    )
                }

                is TaskDetailUiEffect.NavigateToCriteriaEvaluation -> {
                    onNavigateToCriteriaEvaluation(
                        effect.courseId,
                        effect.postId,
                        effect.taskAnswerId,
                    )
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
            }
        }
    }

    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.onEvent(TaskDetailUiEvent.Refresh)
            onRefreshSignalConsumed()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasSeenFirstResume) {
                    viewModel.onEvent(TaskDetailUiEvent.Refresh)
                } else {
                    hasSeenFirstResume = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    TaskDetailContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onPickFromDocuments = { filePicker.launchDocuments() },
        onPickFromGallery = { filePicker.launchGallery() },
    )
}

fun showToast(context: Context, @StringRes text: Int) {
    Toast.makeText(
        context,
        context.getString(text),
        Toast.LENGTH_SHORT,
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun TaskDetailContent(
    state: TaskDetailScreenState,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit = {},
    onPickFromGallery: () -> Unit = {},
) {
    val canEdit = when (state) {
        is TaskDetailScreenState.TeacherView -> state.canEdit
        is TaskDetailScreenState.StudentView -> state.isAuthor
        else -> false
    }
    val canDelete = when (state) {
        is TaskDetailScreenState.TeacherView -> state.isAuthor
        is TaskDetailScreenState.StudentView -> state.isAuthor
        else -> false
    }
    val isRefreshing = when (state) {
        is TaskDetailScreenState.StudentView -> state.isRefreshing
        is TaskDetailScreenState.TeacherView -> state.isRefreshing
        TaskDetailScreenState.Loading -> false
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { onEvent(TaskDetailUiEvent.Refresh) },
    )

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
                actions = {
                    if (canEdit) {
                        IconButton(onClick = { onEvent(TaskDetailUiEvent.EditPost) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit_post_title),
                            )
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = { onEvent(TaskDetailUiEvent.DeletePost) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_post),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
        ) {
            when (state) {
                is TaskDetailScreenState.Loading -> LoadingState()

                is TaskDetailScreenState.StudentView -> StudentViewContent(
                    state = state,
                    onEvent = onEvent,
                    onPickFromDocuments = onPickFromDocuments,
                    onPickFromGallery = onPickFromGallery,
                )

                is TaskDetailScreenState.TeacherView -> TeacherViewContent(
                    state = state,
                    onEvent = onEvent,
                )
            }

            if (state != TaskDetailScreenState.Loading) {
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .padding(top = 12.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun StudentViewContent(
    state: TaskDetailScreenState.StudentView,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelfAssessmentTask = state.task.taskMarkEvaluationType == TaskMarkEvaluationType.SELF_ASSESSMENT
    val canSelfAssess = isSelfAssessmentTask && state.taskAnswerId != null && state.submission != null

    if (state.showSelfAssessmentSheet && state.taskAnswerId != null) {
        SelfAssessmentBottomSheet(
            courseId = state.courseId,
            postId = state.task.id,
            taskAnswerId = state.taskAnswerId,
            onDismiss = { onEvent(TaskDetailUiEvent.DismissSelfAssessment) },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TaskInfoCard(
            task = state.task,
            criteria = state.criteria,
            onDownloadFile = { fileId -> onEvent(TaskDetailUiEvent.DownloadFile(fileId)) },
        )

        if (state.task.postType == "TASK") {
            if (state.submission != null) {
                SubmissionCard(
                        submission = state.submission!!,
                        statusText = state.taskAnswerStatus,
                        showUnsubmit = state.submission!!.score == null || state.submission!!.score == 0,
                        onUnsubmit = { onEvent(TaskDetailUiEvent.UnsubmitWork) },
                )
            } else {
                SubmitWorkCard(
                    taskAnswerFiles = state.taskAnswerFiles,
                    isUploadingFile = state.isUploadingFile,
                    onPickFromDocuments = onPickFromDocuments,
                    onPickFromGallery = onPickFromGallery,
                    onEvent = onEvent,
                )
            }
        }

        if (canSelfAssess) {
            Button(
                onClick = { onEvent(TaskDetailUiEvent.OpenSelfAssessment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.self_assessment_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        StudentCommentsSection(
            selectedTab = state.selectedTab,
            publicComments = state.publicComments,
            privateComments = state.privateComments,
            commentInput = state.commentInput,
            hasPrivateCommentsAccess = state.taskAnswerId != null,
            showTabs = state.task.postType == "TASK",
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TeacherViewContent(
    state: TaskDetailScreenState.TeacherView,
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
        TaskInfoCard(
            task = state.task,
            criteria = state.criteria,
            onEditCriteria = { onEvent(TaskDetailUiEvent.EditCriteria) },
            onDownloadFile = { fileId -> onEvent(TaskDetailUiEvent.DownloadFile(fileId)) },
        )

        TeacherCommentsSection(
            selectedTab = state.selectedTab,
            publicComments = state.publicComments,
            students = state.students,
            maxScore = state.task.maxScore,
            commentInput = state.commentInput,
            evaluateDialog = state.evaluateDialog,
            showTabs = state.task.postType == "TASK",
            canEvaluateByCriteria = state.criteria.isNotEmpty(),
            canEvaluateDirectly = state.task.taskMarkEvaluationType in listOf(
                com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType.TEACHER_DECISION,
                com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType.TEACHER_DECISION_PASS_FAIL,
            ),
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
            state = TaskDetailScreenState.StudentView(
                task = TaskDetail(
                    id = "1",
                    title = "Задание 1: Основы синтаксиса",
                    authorId = "a1",
                    authorName = "Иванов Иван Иванович",
                    createdAt = "17 января, 14:00",
                    description = "Напишите программу, которая выводит \"Hello, World!\" и вычисляет сумму чисел от 1 до 100.",
                    deadline = "20 февраля, 23:59",
                    maxScore = 100,
                    postType = "TASK",
                ),
                submission = Submission(
                    submittedAt = "18 февраля, 15:30",
                    files = listOf("solution1.py"),
                    score = 95,
                    maxScore = 100,
                    isNewGrade = true,
                ),
                taskAnswerId = "ta-1",
                taskAnswerFiles = emptyList(),
                publicComments = emptyList(),
                privateComments = listOf(
                    Comment("1", "Иванов Иван Иванович", "Отличная работа!", "19 февраля, 10:00"),
                    Comment("2", "Сидоров Алексей", "Спасибо!", "19 февраля, 11:00"),
                ),
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
            state = TaskDetailScreenState.TeacherView(
                task = TaskDetail(
                    id = "2",
                    title = "Задание 2: Работа со списками",
                    authorId = "a2",
                    authorName = "Петрова Мария Сергеевна",
                    createdAt = "1 февраля, 10:00",
                    description = "Реализуйте функции для работы со списками: сортировка, поиск элемента, удаление дубликатов.",
                    deadline = "25 февраля, 23:59",
                    maxScore = 100,
                    postType = "TASK",
                ),
                publicComments = emptyList(),
                students = listOf(
                    StudentSubmissionInfo("1", "Сидоров Алексей", "ta-1", null, 100, "OVERDUE"),
                    StudentSubmissionInfo("2", "Козлова Анна", "ta-2", null, 100, "OVERDUE"),
                ),
                commentInput = "",
                selectedTab = TeacherTab.STUDENTS,
                isAuthor = true,
                canEdit = true,
                evaluateDialog = null,
            ),
            onEvent = {},
        )
    }
}
