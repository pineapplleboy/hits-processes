package com.example.googleclass.feature.taskdetail.presentation

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.common.presentation.theme.Warning
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.service.FileUploadService
import org.koin.androidx.compose.koinViewModel

@Composable
fun TaskDetailScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel: TaskDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val activity = context as ComponentActivity
    val filePickerDelegate = remember {
        FilePickerDelegate(
            registry = activity.activityResultRegistry,
            contentResolver = activity.contentResolver,
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

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TaskDetailUiEffect.NavigateBack -> onNavigateBack()

                is TaskDetailUiEffect.NavigateToStudentChat -> {
                    // Navigate to student chat
                }

                is TaskDetailUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is TaskDetailUiEffect.StartFileUpload -> {
                    val intent = Intent(context, FileUploadService::class.java).apply {
                        putParcelableArrayListExtra(
                            FileUploadService.EXTRA_FILE_URIS,
                            ArrayList(effect.uris),
                        )
                    }
                    ContextCompat.startForegroundService(context, intent)
                    Toast.makeText(
                        context,
                        context.getString(R.string.upload_started),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    TaskDetailContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onPickFile = { filePickerDelegate.launch() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailContent(
    state: TaskDetailUiState,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFile: () -> Unit = {},
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
                onPickFile = onPickFile,
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

// region Student View

@Composable
private fun StudentViewContent(
    state: TaskDetailUiState.StudentView,
    onEvent: (TaskDetailUiEvent) -> Unit,
    onPickFile: () -> Unit,
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
                onPickFile = onPickFile,
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

// endregion

// region Teacher View

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

// endregion

// region Shared Components

@Composable
private fun TaskInfoCard(task: TaskDetail) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.task_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = Warning,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${task.authorName} \u00B7 ${task.createdAt}",
                style = MaterialTheme.typography.labelMedium,
                color = MediumGray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stringResource(R.string.deadline_label)} ${task.deadline}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ErrorRed,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${stringResource(R.string.max_score_label)} ${task.maxScore}",
                style = MaterialTheme.typography.labelMedium,
                color = MediumGray,
            )
        }
    }
}

@Composable
private fun SubmissionCard(submission: Submission) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.your_work),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Success),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\u2713",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stringResource(R.string.submitted_label)} ${submission.submittedAt}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.files_label),
                style = MaterialTheme.typography.labelMedium,
                color = MediumGray,
            )

            Spacer(modifier = Modifier.height(4.dp))

            submission.files.forEach { fileName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (submission.score != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.grade_label),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GradeBadge(
                        score = submission.score,
                        maxScore = submission.maxScore,
                        isNew = submission.isNewGrade,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmitWorkCard(
    attachedFiles: List<AttachedFile>,
    onPickFile: () -> Unit,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.your_answer),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            attachedFiles.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = file.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_file),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onEvent(TaskDetailUiEvent.FileRemoved(file.uri)) },
                        tint = MediumGray,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPickFile)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.upload),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MediumGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.attach_file),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onEvent(TaskDetailUiEvent.SubmitWork) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Warning,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.upload),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.submit_work),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GradeBadge(
    score: Int,
    maxScore: Int,
    isNew: Boolean,
) {
    val badgeText = if (isNew) {
        "$score ${stringResource(R.string.out_of)} $maxScore (${stringResource(R.string.new_grade)})"
    } else {
        "$score ${stringResource(R.string.out_of)} $maxScore"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Success)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = badgeText,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// endregion

// region Student Comments

@Composable
private fun StudentCommentsSection(
    selectedTab: StudentTab,
    publicComments: List<Comment>,
    privateComments: List<Comment>,
    commentInput: String,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Tab(
                    selected = selectedTab == StudentTab.PUBLIC_COMMENTS,
                    onClick = { onEvent(TaskDetailUiEvent.StudentTabSelected(StudentTab.PUBLIC_COMMENTS)) },
                    text = {
                        Text(
                            text = stringResource(R.string.public_comments),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == StudentTab.PRIVATE_COMMENTS,
                    onClick = { onEvent(TaskDetailUiEvent.StudentTabSelected(StudentTab.PRIVATE_COMMENTS)) },
                    text = {
                        Text(
                            text = stringResource(R.string.private_comments),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }

            val comments = when (selectedTab) {
                StudentTab.PUBLIC_COMMENTS -> publicComments
                StudentTab.PRIVATE_COMMENTS -> privateComments
            }

            CommentsList(comments = comments)

            val placeholder = when (selectedTab) {
                StudentTab.PUBLIC_COMMENTS -> stringResource(R.string.add_comment_placeholder)
                StudentTab.PRIVATE_COMMENTS -> stringResource(R.string.add_private_comment_placeholder)
            }

            CommentInput(
                value = commentInput,
                placeholder = placeholder,
                onValueChange = { onEvent(TaskDetailUiEvent.CommentInputChanged(it)) },
                onSend = { onEvent(TaskDetailUiEvent.SendComment) },
            )
        }
    }
}

// endregion

// region Teacher Comments

@Composable
private fun TeacherCommentsSection(
    selectedTab: TeacherTab,
    publicComments: List<Comment>,
    students: List<StudentSubmissionInfo>,
    commentInput: String,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Tab(
                    selected = selectedTab == TeacherTab.PUBLIC_COMMENTS,
                    onClick = { onEvent(TaskDetailUiEvent.TeacherTabSelected(TeacherTab.PUBLIC_COMMENTS)) },
                    text = {
                        Text(
                            text = stringResource(R.string.public_comments),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == TeacherTab.STUDENTS,
                    onClick = { onEvent(TaskDetailUiEvent.TeacherTabSelected(TeacherTab.STUDENTS)) },
                    text = {
                        Text(
                            text = stringResource(R.string.students_tab),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }

            when (selectedTab) {
                TeacherTab.PUBLIC_COMMENTS -> {
                    CommentsList(comments = publicComments)

                    CommentInput(
                        value = commentInput,
                        placeholder = stringResource(R.string.add_comment_placeholder),
                        onValueChange = { onEvent(TaskDetailUiEvent.CommentInputChanged(it)) },
                        onSend = { onEvent(TaskDetailUiEvent.SendComment) },
                    )
                }

                TeacherTab.STUDENTS -> {
                    StudentsList(
                        students = students,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

// endregion

// region Comments List & Input

@Composable
private fun CommentsList(comments: List<Comment>) {
    if (comments.isEmpty()) {
        Text(
            text = stringResource(R.string.no_comments),
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
    } else {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            comments.forEach { comment ->
                CommentItem(comment = comment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Column {
        Text(
            text = comment.authorName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = comment.createdAt,
            style = MaterialTheme.typography.labelMedium,
            color = MediumGray,
        )
    }
}

@Composable
private fun CommentInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Warning,
                contentColor = Color.White,
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send_comment),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// endregion

// region Students List

@Composable
private fun StudentsList(
    students: List<StudentSubmissionInfo>,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        students.forEach { student ->
            StudentItem(
                student = student,
                onOpenChat = { onEvent(TaskDetailUiEvent.OpenStudentChat(student.studentId)) },
            )
        }
    }
}

@Composable
private fun StudentItem(
    student: StudentSubmissionInfo,
    onOpenChat: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = student.studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                StatusBadge(status = student.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            val scoreText = if (student.score != null) {
                "${student.score} ${stringResource(R.string.out_of)} ${student.maxScore} ${
                    stringResource(
                        R.string.points_suffix
                    )
                }"
            } else {
                "\u2014 ${stringResource(R.string.out_of)} ${student.maxScore} ${stringResource(R.string.points_suffix)}"
            }
            Text(
                text = scoreText,
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(onClick = onOpenChat)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.chat),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MediumGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.open_student_chat),
                    style = MaterialTheme.typography.labelLarge,
                    color = MediumGray,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: SubmissionStatus) {
    val (text, backgroundColor) = when (status) {
        SubmissionStatus.SUBMITTED -> stringResource(R.string.status_submitted) to Success
        SubmissionStatus.OVERDUE -> stringResource(R.string.status_overdue) to ErrorRed
        SubmissionStatus.PENDING -> stringResource(R.string.status_pending) to Warning
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

// endregion

// region Previews

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
                    StudentSubmissionInfo(
                        "1",
                        "Сидоров Алексей",
                        null,
                        100,
                        SubmissionStatus.OVERDUE
                    ),
                    StudentSubmissionInfo("2", "Козлова Анна", null, 100, SubmissionStatus.OVERDUE),
                    StudentSubmissionInfo(
                        "3",
                        "Смирнов Дмитрий",
                        null,
                        100,
                        SubmissionStatus.OVERDUE
                    ),
                ),
                commentInput = "",
                selectedTab = TeacherTab.STUDENTS,
            ),
            onEvent = {},
        )
    }
}

// endregion
