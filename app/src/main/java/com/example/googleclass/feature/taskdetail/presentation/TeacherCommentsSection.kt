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
import com.example.googleclass.common.presentation.component.CommentsSection
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun TeacherCommentsSection(
    selectedTab: TeacherTab,
    publicComments: List<Comment>,
    students: List<StudentSubmissionInfo>,
    maxScore: Int,
    commentInput: String,
    evaluateDialog: EvaluateDialogState?,
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
                    CommentsSection(
                        comments = publicComments.map { it.toUiModel() },
                        inputValue = commentInput,
                        placeholder = stringResource(R.string.add_comment_placeholder),
                        onInputChange = { onEvent(TaskDetailUiEvent.CommentInputChanged(it)) },
                        onSend = { onEvent(TaskDetailUiEvent.SendComment) },
                    )
                }

                TeacherTab.STUDENTS -> {
                    StudentsList(
                        students = students,
                        maxScore = maxScore,
                        onEvent = onEvent,
                    )
                }
            }

            evaluateDialog?.let { dialog ->
                EvaluateDialog(
                    studentName = dialog.studentName,
                    maxScore = dialog.maxScore,
                    score = dialog.score,
                    onScoreChange = { onEvent(TaskDetailUiEvent.SetEvaluateScore(it)) },
                    onDismiss = { onEvent(TaskDetailUiEvent.DismissEvaluateDialog) },
                    onConfirm = { onEvent(TaskDetailUiEvent.SubmitEvaluate) },
                )
            }
        }
    }
}

@Composable
private fun EvaluateDialog(
    studentName: String,
    maxScore: Int,
    score: Int,
    onScoreChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var scoreText by remember { mutableStateOf(score.toString())}
    LaunchedEffect(score) {
        scoreText = score.toString()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оценить: $studentName") },
        text = {
            Column {
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { new ->
                        val filtered = new.filter { it.isDigit() }.take(4)
                        scoreText = filtered
                        onScoreChange(filtered.toIntOrNull() ?: 0)
                    },
                    label = { Text("Баллы (0–$maxScore)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
