package com.example.googleclass.feature.course.presentation

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
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.components.AssignmentStatusBadge
import com.example.googleclass.common.presentation.components.CardHeaderWithIcon
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.CommentInputField
import com.example.googleclass.common.presentation.components.CommentItem
import com.example.googleclass.common.presentation.components.ContentDivider
import com.example.googleclass.common.presentation.components.CreateFAB
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.components.FileChip
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.common.presentation.components.PublicationTypeLabel
import androidx.compose.ui.tooling.preview.Preview
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.feature.course.domain.model.AssignmentStatus
import com.example.googleclass.feature.course.domain.model.AssignmentStatusInfo
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.CourseParticipant
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.PublicationType
import com.example.googleclass.feature.course.domain.model.Submission
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CourseScreen(
    course: Course,
    currentUser: User,
    isTeacher: Boolean,
    publications: List<Publication>,
    submissions: List<Submission>,
    users: Map<String, User>,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onNavigateBack: () -> Unit,
    onAssignmentClick: (String) -> Unit,
    onCreatePublication: (PublicationType, String, String, Date?) -> Unit,
    onAddComment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_stream),
        stringResource(R.string.tab_participants)
    )

    Scaffold(
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
                    publications = publications,
                    submissions = submissions,
                    users = users,
                    currentUser = currentUser,
                    isTeacher = isTeacher,
                    getAssignmentStatus = getAssignmentStatus,
                    onAssignmentClick = onAssignmentClick,
                    onCreatePublication = onCreatePublication,
                    onAddComment = onAddComment
                )
                1 -> ParticipantsTab(
                    course = course,
                    users = users
                )
            }
        }
    }
}

@Composable
private fun StreamTab(
    publications: List<Publication>,
    submissions: List<Submission>,
    users: Map<String, User>,
    currentUser: User,
    isTeacher: Boolean,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onAssignmentClick: (String) -> Unit,
    onCreatePublication: (PublicationType, String, String, Date?) -> Unit,
    onAddComment: (String, String) -> Unit
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
                        currentUser = currentUser,
                        isTeacher = isTeacher,
                        getAssignmentStatus = getAssignmentStatus,
                        onAssignmentClick = onAssignmentClick,
                        onAddComment = onAddComment,
                        dateFormat = dateFormat
                    )
                }
            }
        }

        if (isTeacher) {
            CreateFAB(
                onClick = { showCreateDialog = true },
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
                onCreatePublication(type, title, text, deadline)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun PublicationCard(
    publication: Publication,
    users: Map<String, User>,
    currentUser: User,
    isTeacher: Boolean,
    getAssignmentStatus: (String) -> AssignmentStatusInfo,
    onAssignmentClick: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    dateFormat: SimpleDateFormat
) {
    var commentText by remember { mutableStateOf("") }

    InfoCard(
        onClick = if (publication.type == PublicationType.ASSIGNMENT) {
            { onAssignmentClick(publication.id) }
        } else null
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
            subtitle = "${users[publication.authorId]?.name} · ${dateFormat.format(publication.createdAt)}",
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

        if (publication.text != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = publication.text,
                style = MaterialTheme.typography.bodyMedium
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

        if (publication.type == PublicationType.ANNOUNCEMENT) {
            Spacer(modifier = Modifier.height(8.dp))
            ContentDivider()

            publication.comments?.let { comments ->
                if (comments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        comments.forEach { comment ->
                            CommentItem(
                                authorName = users[comment.userId]?.name ?: "",
                                text = comment.text,
                                timestamp = dateFormat.format(comment.createdAt)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            CommentInputField(
                value = commentText,
                onValueChange = { commentText = it },
                onSend = {
                    onAddComment(publication.id, commentText)
                    commentText = ""
                }
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
    users: Map<String, User>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(course.participants) { participant ->
            val user = users[participant.userId]
            if (user != null) {
                InfoCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = when (participant.role) {
                                    UserRole.MAIN_TEACHER -> stringResource(R.string.role_head_teacher)
                                    UserRole.TEACHER -> stringResource(R.string.role_teacher)
                                    UserRole.STUDENT -> stringResource(R.string.role_student)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Экран курса")
@Composable
private fun CourseScreenPreview() {
    GoogleClassTheme {
        val statusText = stringResource(R.string.assignment_status_not_submitted)
        CourseScreen(
            course = Course(
                id = "1",
                name = "Математический анализ",
                participants = listOf(
                    CourseParticipant("u1", UserRole.MAIN_TEACHER),
                    CourseParticipant("u2", UserRole.STUDENT),
                    CourseParticipant("u3", UserRole.STUDENT),
                )
            ),
            currentUser = User("u1", "Иван Петров", "teacher@example.com"),
            isTeacher = true,
            publications = listOf(
                Publication(
                    id = "p1",
                    type = PublicationType.ANNOUNCEMENT,
                    title = "Добро пожаловать на курс",
                    text = "Первое занятие состоится в понедельник.",
                    authorId = "u1",
                    createdAt = Date(),
                    comments = emptyList()
                ),
                Publication(
                    id = "p2",
                    type = PublicationType.ASSIGNMENT,
                    title = "Домашнее задание 1",
                    text = "Решить задачи из учебника, стр. 10–15.",
                    authorId = "u1",
                    createdAt = Date(),
                    deadline = Date(System.currentTimeMillis() + 86400000 * 7)
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
            onAssignmentClick = { },
            onCreatePublication = { _, _, _, _ -> },
            onAddComment = { _, _ -> }
        )
    }
}
