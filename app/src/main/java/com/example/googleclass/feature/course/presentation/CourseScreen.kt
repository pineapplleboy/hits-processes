package com.example.googleclass.feature.course.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.AssignmentStatusBadge
import com.example.googleclass.common.presentation.components.CardHeaderWithIcon
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.ContentDivider
import com.example.googleclass.common.presentation.components.CreateFAB
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.components.FileChip
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.common.presentation.components.PublicationTypeLabel
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.feature.course.domain.model.AssignmentStatus
import com.example.googleclass.feature.course.domain.model.AssignmentStatusInfo
import com.example.googleclass.feature.course.domain.model.Comment
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.CourseParticipant
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.PublicationType
import com.example.googleclass.feature.course.domain.model.Submission
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CourseScreenRoute(
    courseId: String,
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePublicationClick: () -> Unit,
) {
    val viewModel: CourseDetailViewModel = koinViewModel(parameters = { parametersOf(courseId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is CourseDetailUiState.Loading -> LoadingState()
        is CourseDetailUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        is CourseDetailUiState.Content -> {
            CourseScreen(
                courseId = courseId,
                course = state.course,
                currentUser = state.currentUser,
                isTeacher = state.isTeacher,
                isMainTeacher = state.isMainTeacher,
                publications = state.publications,
                submissions = emptyList(),
                users = state.users,
                getAssignmentStatus = { assignmentId ->
                    // TODO: replace with real status from task answers
                    AssignmentStatusInfo(
                        status = AssignmentStatus.PENDING,
                        text = "",
                        grade = null,
                        maxScore = state.publications
                            .firstOrNull { it.id == assignmentId }?.maxScore ?: 100,
                    )
                },
                onNavigateBack = onNavigateBack,
                onPostClick = onPostClick,
                onCreatePublication = onCreatePublicationClick,
            )
        }
    }
}

@Composable
fun CourseScreen(
    courseId: String,
    course: Course,
    currentUser: User,
    isTeacher: Boolean,
    isMainTeacher: Boolean,
    publications: List<Publication>,
    submissions: List<Submission>,
    users: Map<String, User>,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePublication: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_stream),
        stringResource(R.string.tab_participants)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = course.name,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CourseInfoBlock(
                course = course,
                isTeacher = isTeacher
            )
            Spacer(modifier = Modifier.height(12.dp))
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> StreamTab(
                    course = course,
                    publications = publications,
                    submissions = submissions,
                    users = users,
                    currentUser = currentUser,
                    isTeacher = isTeacher,
                    getAssignmentStatus = getAssignmentStatus,
                    onCreatePublication = onCreatePublication,
                    onPostClick = onPostClick,
                )
                1 -> ParticipantsTab(
                    course = course,
                    users = users,
                    isMainTeacher = isMainTeacher,
                )
            }
        }
    }
}

@Composable
private fun StreamTab(
    course: Course,
    publications: List<Publication>,
    submissions: List<Submission>,
    users: Map<String, User>,
    currentUser: User,
    isTeacher: Boolean,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onCreatePublication: () -> Unit,
    onPostClick: (String) -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("d MMMM, HH:mm", Locale("ru")) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (publications.isEmpty()) {
                item {
                    EmptyState(message = stringResource(R.string.empty_publications))
                }
            } else {
                items(publications) { publication ->
                    PublicationCard(
                        publication = publication,
                        users = users,
                        isTeacher = isTeacher,
                        getAssignmentStatus = getAssignmentStatus,
                        onPostClick = onPostClick,
                        dateFormat = dateFormat
                    )
                }
            }
        }

        if (isTeacher) {
            CreateFAB(
                onClick = {
                    showCreateDialog = false
                    onCreatePublication()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    if (showCreateDialog) {
        CreatePublicationDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { type, title, text, deadline ->
                onCreatePublication()
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CourseInfoBlock(
    course: Course,
    isTeacher: Boolean
) {
    InfoCard(
        onClick = { },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!course.description.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.course_description_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.small,
                color = if (course.isArchived) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (course.isArchived) stringResource(R.string.course_archived)
                    else stringResource(R.string.course_active),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (course.isArchived) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            if (isTeacher && !course.joinCode.isNullOrBlank()) {
                androidx.compose.material3.Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = course.joinCode,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicationCard(
    publication: Publication,
    users: Map<String, User>,
    isTeacher: Boolean,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onPostClick: (String) -> Unit,
    dateFormat: SimpleDateFormat
) {
    val authorName = users[publication.authorId]?.name ?: ""
    val commentCount = publication.comments?.size ?: 0

    InfoCard(
        onClick = { onPostClick(publication.id) },
    ) {
        CardHeaderWithIcon(
            icon = {
                Icon(
                    painter = painterResource(
                        when (publication.type) {
                            PublicationType.ANNOUNCEMENT -> R.drawable.ic_notifications
                            PublicationType.ASSIGNMENT -> R.drawable.ic_assignment
                            PublicationType.MATERIAL -> R.drawable.ic_description
                        }
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = publication.title,
            subtitle = "$authorName · ${dateFormat.format(publication.createdAt)}",
            badge = {
                PublicationTypeLabel(
                    type = when (publication.type) {
                        PublicationType.ANNOUNCEMENT -> stringResource(R.string.publication_type_announcement)
                        PublicationType.ASSIGNMENT -> stringResource(R.string.publication_type_assignment)
                        PublicationType.MATERIAL -> stringResource(R.string.publication_type_material)
                    }
                )
            }
        )

        if (!publication.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = publication.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        publication.files?.let { files ->
            if (files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    files.forEach { file ->
                        FileChip(fileName = file)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = stringResource(R.string.comments_count, commentCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (publication.type == PublicationType.ASSIGNMENT) {
            publication.maxScore?.let { max ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.score_format_no_grade, max),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (publication.deadline != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_schedule),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.deadline_label, dateFormat.format(publication.deadline)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (publication.type == PublicationType.ASSIGNMENT && !isTeacher) {
            Spacer(modifier = Modifier.height(8.dp))
            ContentDivider()
            val status = getAssignmentStatus(publication.id)
            AssignmentStatusBadge(
                status = status.text,
                icon = {
                    Icon(
                        painter = painterResource(
                            when (status.status) {
                                AssignmentStatus.SUBMITTED -> R.drawable.ic_check_circle
                                AssignmentStatus.OVERDUE -> R.drawable.ic_cancel
                                AssignmentStatus.PENDING -> R.drawable.ic_schedule
                            }
                        ),
                        contentDescription = null,
                        tint = when (status.status) {
                            AssignmentStatus.SUBMITTED -> MaterialTheme.colorScheme.tertiary
                            AssignmentStatus.OVERDUE -> MaterialTheme.colorScheme.error
                            AssignmentStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                grade = status.grade,
                maxScore = status.maxScore
            )
        }
    }
}

@Composable
private fun CreatePublicationDialog(
    onDismiss: () -> Unit,
    onCreate: (PublicationType, String, String, Date?) -> Unit
) {
    var selectedType by remember { mutableStateOf(PublicationType.ANNOUNCEMENT) }
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var deadlineText by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_publication)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(stringResource(R.string.publication_type_label), style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.FilterChip(
                            selected = selectedType == PublicationType.ANNOUNCEMENT,
                            onClick = { selectedType = PublicationType.ANNOUNCEMENT },
                            label = { Text(stringResource(R.string.publication_type_announcement)) }
                        )
                        androidx.compose.material3.FilterChip(
                            selected = selectedType == PublicationType.ASSIGNMENT,
                            onClick = { selectedType = PublicationType.ASSIGNMENT },
                            label = { Text(stringResource(R.string.publication_type_assignment)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.post_title)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.post_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (selectedType == PublicationType.ASSIGNMENT) {
                    OutlinedTextField(
                        value = deadlineText,
                        onValueChange = { deadlineText = it },
                        label = { Text(stringResource(R.string.deadline_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val deadline = if (selectedType == PublicationType.ASSIGNMENT && deadlineText.isNotBlank()) {
                            Date()
                        } else null
                        onCreate(selectedType, title, text, deadline)
                    }
                }
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ParticipantsTab(
    course: Course,
    users: Map<String, User>,
    isMainTeacher: Boolean,
) {
    val teachers = course.participants.filter {
        it.role == UserRole.MAIN_TEACHER || it.role == UserRole.TEACHER
    }
    val students = course.participants.filter { it.role == UserRole.STUDENT }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InfoCard(onClick = { }) {
                Text(
                    text = "Преподаватели (${teachers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    teachers.forEach { participant ->
                        val user = users[participant.userId] ?: return@forEach
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = when (participant.role) {
                                        UserRole.MAIN_TEACHER -> stringResource(R.string.role_head_teacher)
                                        UserRole.TEACHER -> stringResource(R.string.role_teacher)
                                        UserRole.STUDENT -> stringResource(R.string.role_student)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isMainTeacher && participant.role != UserRole.MAIN_TEACHER) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    androidx.compose.material3.IconButton(
                                        onClick = { /* TODO: promote */ },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_arrow_upward),
                                            contentDescription = null,
                                            tint = Success,
                                        )
                                    }
                                    androidx.compose.material3.IconButton(
                                        onClick = { /* TODO: demote */ },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_arrow_downward),
                                            contentDescription = null,
                                            tint = ErrorRed,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            InfoCard(onClick = { }) {
                Text(
                    text = "Студенты (${students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (students.isEmpty()) {
                    Text(
                        text = "Пока нет студентов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        students.forEach { participant ->
                            val user = users[participant.userId] ?: return@forEach
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (isMainTeacher) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        androidx.compose.material3.IconButton(
                                            onClick = { /* TODO: promote */ },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_arrow_upward),
                                                contentDescription = null,
                                                tint = Success,
                                            )
                                        }
                                        androidx.compose.material3.IconButton(
                                            onClick = { /* TODO: demote */ },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_arrow_downward),
                                                contentDescription = null,
                                                tint = ErrorRed,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Экран курса (преподаватель)")
@Composable
private fun CourseScreenPreview() {
    GoogleClassTheme {
        val statusText = stringResource(R.string.assignment_status_not_submitted)
        CourseScreen(
            courseId = "1",
            course = Course(
                id = "1",
                name = "Математический анализ",
                description = "Курс для первого курса бакалавриата. Разбор тем: пределы, производные, интегралы.",
                joinCode = "ABC-123",
                isArchived = false,
                participants = listOf(
                    CourseParticipant("u1", UserRole.MAIN_TEACHER),
                    CourseParticipant("u2", UserRole.STUDENT),
                    CourseParticipant("u3", UserRole.STUDENT),
                )
            ),
            currentUser = User("u1", "Иван Петров", "teacher@example.com"),
            isTeacher = true,
            isMainTeacher = true,
            publications = listOf(
                Publication(
                    id = "p1",
                    type = PublicationType.ANNOUNCEMENT,
                    title = "Добро пожаловать на курс",
                    text = "Первое занятие состоится в понедельник.",
                    authorId = "u1",
                    createdAt = Date(),
                    comments = listOf(Comment("u2", "Спасибо!", Date())),
                    maxScore = null
                ),
                Publication(
                    id = "p2",
                    type = PublicationType.ASSIGNMENT,
                    title = "Домашнее задание 1",
                    text = "Решить задачи из учебника, стр. 10–15.",
                    authorId = "u1",
                    createdAt = Date(),
                    deadline = Date(System.currentTimeMillis() + 86400000 * 7),
                    maxScore = 100
                ),
            ),
            submissions = emptyList(),
            users = mapOf(
                "u1" to User("u1", "Иван Петров", "teacher@example.com"),
                "u2" to User("u2", "Мария Сидорова", "student@example.com"),
                "u3" to User("u3", "Алексей Козлов", "student2@example.com"),
            ),
            getAssignmentStatus = { AssignmentStatusInfo(AssignmentStatus.PENDING, statusText, null, 100) },
            onNavigateBack = { },
            onPostClick = { },
            onCreatePublication = { },
        )
    }
}

@Preview(showBackground = true, name = "Участники курса")
@Composable
private fun ParticipantsTabPreview() {
    GoogleClassTheme {
        ParticipantsTab(
            course = Course(
                id = "1",
                name = "Математический анализ",
                participants = listOf(
                    CourseParticipant("u1", UserRole.MAIN_TEACHER),
                    CourseParticipant("u2", UserRole.TEACHER),
                    CourseParticipant("u3", UserRole.STUDENT),
                    CourseParticipant("u4", UserRole.STUDENT),
                )
            ),
            users = mapOf(
                "u1" to User("u1", "Иван Петров", "teacher@example.com"),
                "u2" to User("u2", "Анна Смирнова", "anna@example.com"),
                "u3" to User("u3", "Мария Сидорова", "student@example.com"),
                "u4" to User("u4", "Алексей Козлов", "student2@example.com"),
            ),
            isMainTeacher = true,
        )
    }
}
