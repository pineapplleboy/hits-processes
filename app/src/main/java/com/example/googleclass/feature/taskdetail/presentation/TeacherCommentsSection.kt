package com.example.googleclass.feature.taskdetail.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo

@Composable
internal fun TeacherCommentsSection(
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
