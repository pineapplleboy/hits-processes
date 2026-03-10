package com.example.googleclass.feature.courses.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.common.presentation.theme.White
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onCourseClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: CoursesScreenViewModel = koinViewModel(),
) {
    // Когда logout завершился — переходим на экран авторизации
    LaunchedEffect(viewModel.logoutCompleted) {
        if (viewModel.logoutCompleted) onLogoutClick()
    }

    CoursesScreenContent(
        state = viewModel.state,
        createDialogState = viewModel.dialogState,
        joinDialogState = viewModel.joinDialogState,
        onCourseClick = onCourseClick,
        onTaskClick = onTaskClick,
        onLogoutAction = viewModel::logout,
        onProfileClick = onProfileClick,
        onCreateCourseClick = viewModel::openCreateCourseDialog,
        onDismissCreateDialog = viewModel::dismissCreateCourseDialog,
        onCourseNameChanged = viewModel::onCourseNameChanged,
        onCourseDescriptionChanged = viewModel::onCourseDescriptionChanged,
        onSubmitCreateCourse = viewModel::submitCreateCourse,
        onJoinCourseClick = viewModel::openJoinCourseDialog,
        onDismissJoinDialog = viewModel::dismissJoinCourseDialog,
        onJoinCodeChanged = viewModel::onJoinCodeChanged,
        onSubmitJoinCourse = viewModel::submitJoinCourse,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreenContent(
    state: CoursesScreenState,
    createDialogState: CreateCourseDialogState?,
    joinDialogState: JoinCourseDialogState?,
    onCourseClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onLogoutAction: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateCourseClick: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onCourseNameChanged: (String) -> Unit,
    onCourseDescriptionChanged: (String) -> Unit,
    onSubmitCreateCourse: () -> Unit,
    onJoinCourseClick: () -> Unit,
    onDismissJoinDialog: () -> Unit,
    onJoinCodeChanged: (String) -> Unit,
    onSubmitJoinCourse: () -> Unit,
) {
    when (state) {
        is CoursesScreenState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is CoursesScreenState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                )
            }
        }

        is CoursesScreenState.Content -> {
            CoursesContentBody(
                state = state,
                onCourseClick = onCourseClick,
                onTaskClick = onTaskClick,
                onLogoutAction = onLogoutAction,
                onProfileClick = onProfileClick,
                onCreateCourseClick = onCreateCourseClick,
                onJoinCourseClick = onJoinCourseClick,
            )
        }
    }

    if (createDialogState != null) {
        CreateCourseDialog(
            dialogState = createDialogState,
            onDismiss = onDismissCreateDialog,
            onNameChanged = onCourseNameChanged,
            onDescriptionChanged = onCourseDescriptionChanged,
            onConfirm = onSubmitCreateCourse,
        )
    }

    if (joinDialogState != null) {
        JoinCourseDialog(
            dialogState = joinDialogState,
            onDismiss = onDismissJoinDialog,
            onCodeChanged = onJoinCodeChanged,
            onConfirm = onSubmitJoinCourse,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoursesContentBody(
    state: CoursesScreenState.Content,
    onCourseClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onLogoutAction: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateCourseClick: () -> Unit,
    onJoinCourseClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.courses_tab_courses),
        stringResource(R.string.courses_tab_tasks),
    )
    var showActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CoursesTopBar(userName = state.userName, onProfileClick = onProfileClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showActionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_course_action_title),
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }

            when (selectedTab) {
                0 -> CoursesList(
                    courses = state.courses,
                    onCourseClick = onCourseClick,
                )

                1 -> TasksList(
                    tasks = state.tasks,
                    onTaskClick = onTaskClick,
                )
            }
        }
    }

    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            CourseActionSheetContent(
                onJoinCourseClick = {
                    showActionSheet = false
                    onJoinCourseClick()
                },
                onCreateCourseClick = {
                    showActionSheet = false
                    onCreateCourseClick()
                },
                onLogoutClick = {
                    showActionSheet = false
                    onLogoutAction()
                },
            )
        }
    }
}

// region Action Bottom Sheet

@Composable
private fun CourseActionSheetContent(
    onJoinCourseClick: () -> Unit,
    onCreateCourseClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        HorizontalDivider()

        ActionSheetItem(
            iconRes = R.drawable.ic_person,
            text = stringResource(R.string.menu_join_course),
            onClick = onJoinCourseClick,
        )

        ActionSheetItem(
            iconRes = R.drawable.ic_add,
            text = stringResource(R.string.menu_create_course),
            onClick = onCreateCourseClick,
        )

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        ActionSheetItem(
            iconRes = R.drawable.ic_logout,
            text = stringResource(R.string.logout_action),
            onClick = onLogoutClick,
            tint = ErrorRed,
            textColor = ErrorRed,
        )
    }
}

@Composable
private fun ActionSheetItem(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

// endregion

// region Top Bar

@Composable
private fun CoursesTopBar(
    userName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_school),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.courses_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFE8A048),
                        shape = CircleShape,
                    )
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center,
            ) {
                val initials = userName.split(" ")
                    .mapNotNull { it.firstOrNull() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// endregion

// region Courses Tab

@Composable
private fun CoursesList(
    courses: List<CourseUiItem>,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = courses,
            key = { it.id },
        ) { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course.id) },
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: CourseUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = course.subject,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryBlue,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = course.role,
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryBlue,
            )
            if (course.code != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// endregion

// region Tasks Tab

@Composable
private fun TasksList(
    tasks: List<TaskUiItem>,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = tasks,
            key = { it.id },
        ) { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task.id) },
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (task.status) {
                    TaskStatus.SUBMITTED -> stringResource(R.string.task_status_submitted)
                    TaskStatus.OVERDUE -> stringResource(R.string.task_status_overdue)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (task.score != null && task.maxScore != null) {
                    "${task.score}/${task.maxScore}"
                } else {
                    stringResource(R.string.no_score)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val statusColor = when (task.status) {
                TaskStatus.SUBMITTED -> Success
                TaskStatus.OVERDUE -> ErrorRed
            }
            val statusIcon = when (task.status) {
                TaskStatus.SUBMITTED -> R.drawable.ic_check_circle
                TaskStatus.OVERDUE -> R.drawable.ic_cancel
            }
            Icon(
                painter = painterResource(statusIcon),
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.deadline_format, task.deadline),
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText,
            )
        }
    }
}

// endregion

// region Create Course Dialog

@Composable
private fun CreateCourseDialog(
    dialogState: CreateCourseDialogState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.create_course_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dialogState.name,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.course_name_hint)) },
                    singleLine = true,
                    enabled = !dialogState.isCreating,
                    isError = dialogState.error != null && dialogState.name.trim().length < 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dialogState.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text(stringResource(R.string.course_description_hint)) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    enabled = !dialogState.isCreating,
                    isError = dialogState.error != null && dialogState.description.trim().length < 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (dialogState.error != null) {
                    Text(
                        text = dialogState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                    )
                }
                if (dialogState.isCreating) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !dialogState.isCreating) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !dialogState.isCreating) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

// endregion

// region Join Course Dialog

@Composable
private fun JoinCourseDialog(
    dialogState: JoinCourseDialogState,
    onDismiss: () -> Unit,
    onCodeChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.join_course_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dialogState.code,
                    onValueChange = onCodeChanged,
                    label = { Text(stringResource(R.string.join_course_code_hint)) },
                    singleLine = true,
                    enabled = !dialogState.isJoining,
                    isError = dialogState.error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (dialogState.error != null) {
                    Text(
                        text = dialogState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                    )
                }
                if (dialogState.isJoining) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !dialogState.isJoining) {
                Text(stringResource(R.string.join))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !dialogState.isJoining) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

// endregion

// region Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Курсы — контент")
@Composable
private fun CoursesContentPreview() {
    GoogleClassTheme {
        CoursesScreenContent(
            state = CoursesScreenState.Content(
                courses = listOf(
                    CourseUiItem("1", "Программирование на Python", "Информатика", "Студент"),
                    CourseUiItem("2", "Веб-разработка", "Информатика", "Студент"),
                ),
                tasks = listOf(
                    TaskUiItem("1", "Задание 1: Основы синтаксиса", TaskStatus.SUBMITTED, "95", "100", "20.02.2026"),
                    TaskUiItem("2", "Задание 2: Работа со списками", TaskStatus.OVERDUE, null, null, "25.02.2026"),
                ),
                userName = "Сидоров Алексей",
            ),
            createDialogState = null,
            joinDialogState = null,
            onCourseClick = {},
            onTaskClick = {},
            onLogoutAction = {},
            onProfileClick = {},
            onCreateCourseClick = {},
            onDismissCreateDialog = {},
            onCourseNameChanged = {},
            onCourseDescriptionChanged = {},
            onSubmitCreateCourse = {},
            onJoinCourseClick = {},
            onDismissJoinDialog = {},
            onJoinCodeChanged = {},
            onSubmitJoinCourse = {},
        )
    }
}

@Preview(showBackground = true, name = "Курсы — диалог создания")
@Composable
private fun CoursesCreateDialogPreview() {
    GoogleClassTheme {
        CreateCourseDialog(
            dialogState = CreateCourseDialogState(name = "Матанализ", description = ""),
            onDismiss = {},
            onNameChanged = {},
            onDescriptionChanged = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true, name = "Курсы — диалог присоединения")
@Composable
private fun CoursesJoinDialogPreview() {
    GoogleClassTheme {
        JoinCourseDialog(
            dialogState = JoinCourseDialogState(code = "ABC-123"),
            onDismiss = {},
            onCodeChanged = {},
            onConfirm = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Курсы — загрузка")
@Composable
private fun CoursesLoadingPreview() {
    GoogleClassTheme {
        CoursesScreenContent(
            state = CoursesScreenState.Loading,
            createDialogState = null,
            joinDialogState = null,
            onCourseClick = {},
            onTaskClick = {},
            onLogoutAction = {},
            onProfileClick = {},
            onCreateCourseClick = {},
            onDismissCreateDialog = {},
            onCourseNameChanged = {},
            onCourseDescriptionChanged = {},
            onSubmitCreateCourse = {},
            onJoinCourseClick = {},
            onDismissJoinDialog = {},
            onJoinCodeChanged = {},
            onSubmitJoinCourse = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Курсы — ошибка")
@Composable
private fun CoursesErrorPreview() {
    GoogleClassTheme {
        CoursesScreenContent(
            state = CoursesScreenState.Error("Не удалось загрузить курсы"),
            createDialogState = null,
            joinDialogState = null,
            onCourseClick = {},
            onTaskClick = {},
            onLogoutAction = {},
            onProfileClick = {},
            onCreateCourseClick = {},
            onDismissCreateDialog = {},
            onCourseNameChanged = {},
            onCourseDescriptionChanged = {},
            onSubmitCreateCourse = {},
            onJoinCourseClick = {},
            onDismissJoinDialog = {},
            onJoinCodeChanged = {},
            onSubmitJoinCourse = {},
        )
    }
}

// endregion
