package com.example.googleclass.feature.taskdetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.common.presentation.theme.WarningOrange
import com.example.googleclass.feature.taskdetail.domain.model.StudentCriteriaScoreInfo
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo

@Composable
internal fun StudentsList(
    students: List<StudentSubmissionInfo>,
    maxScore: Int,
    canEvaluateByCriteria: Boolean = false,
    canEvaluateDirectly: Boolean = true,
    onEvent: (TaskDetailUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        students.forEach { student ->
            StudentItem(
                student = student,
                maxScore = maxScore,
                canEvaluateByCriteria = canEvaluateByCriteria,
                canEvaluateDirectly = canEvaluateDirectly,
                onDownloadFile = { fileId -> onEvent(TaskDetailUiEvent.DownloadFile(fileId)) },
                onOpenChat = {
                    onEvent(
                        TaskDetailUiEvent.OpenStudentChat(
                            taskAnswerId = student.taskAnswerId,
                            studentName = student.studentName,
                            studentUserId = student.studentId,
                        ),
                    )
                },
                onEvaluate = {
                    if (canEvaluateByCriteria) {
                        onEvent(TaskDetailUiEvent.OpenCriteriaEvaluation(student.taskAnswerId))
                    } else {
                        onEvent(
                            TaskDetailUiEvent.EvaluateStudent(
                                taskAnswerId = student.taskAnswerId,
                                studentName = student.studentName,
                                maxScore = student.maxScore,
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
internal fun StudentItem(
    student: StudentSubmissionInfo,
    maxScore: Int,
    canEvaluateByCriteria: Boolean = true,
    canEvaluateDirectly: Boolean = true,
    onDownloadFile: (fileId: String) -> Unit,
    onOpenChat: () -> Unit,
    onEvaluate: () -> Unit,
) {
    val isEvaluated = isEvaluatedStatus(student.evaluationStatus)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(status = student.status)
                    EvaluationStatusBadge(evaluationStatus = student.evaluationStatus)
                }
            }

            if (student.files.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.files_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MediumGray,
                    )
                    student.files.forEach { file ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDownloadFile(file.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                androidx.compose.material3.Icon(
                                    painter = painterResource(R.drawable.ic_description),
                                    contentDescription = null,
                                    tint = MediumGray,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = file.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = if (isEvaluated && student.score != null) {
                    stringResource(
                        R.string.score_format,
                        student.score,
                        student.maxScore.takeIf { it > 0 } ?: maxScore,
                    )
                } else {
                    stringResource(R.string.not_graded)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
            )

            if (canEvaluateByCriteria || canEvaluateDirectly) {
                Button(
                    onClick = onEvaluate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF090A1F),
                        contentColor = Color.White,
                    ),
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_assignment),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (canEvaluateByCriteria) {
                                if (student.hasCriteriaEvaluation) {
                                    R.string.criteria_evaluation_edit_button
                                } else {
                                    R.string.criteria_evaluation_start_button
                                }
                            } else {
                                R.string.evaluate_work
                            },
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (student.criteriaScores.isNotEmpty()) {
                CriteriaScoreSummaryCard(criteriaScores = student.criteriaScores)
            }

            OutlinedButton(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.chat),
                    contentDescription = null,
                    tint = MediumGray,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.criteria_evaluation_chat_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
internal fun CriteriaScoreSummaryCard(
    criteriaScores: List<StudentCriteriaScoreInfo>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEAF9EC),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.criteria_evaluation_summary_title),
                style = MaterialTheme.typography.labelLarge,
                color = Success,
                fontWeight = FontWeight.SemiBold,
            )
            criteriaScores.forEach { criterion ->
                Text(
                    text = stringResource(
                        R.string.criteria_evaluation_summary_item,
                        criterion.name,
                        criterion.score?.let(::formatScore) ?: "–",
                        formatScore(criterion.maxScore),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun StatusBadge(status: String) {
    val upper = status.uppercase()
    val (text, backgroundColor) = when (upper) {
        "SUBMITTED", "COMPLETED" ->
            stringResource(R.string.criteria_evaluation_status_submitted) to Success

        "COMPLETED_AFTER_DEADLINE", "COMPETED_AFTER_DEADLINE" ->
            stringResource(R.string.criteria_evaluation_status_late) to WarningOrange

        "NOT_COMPLETED" ->
            stringResource(R.string.criteria_evaluation_status_not_submitted) to ErrorRed

        "NEW" ->
            stringResource(R.string.criteria_evaluation_status_new) to PrimaryBlue

        else ->
            (status.ifBlank { stringResource(R.string.criteria_evaluation_status_unknown) }) to MediumGray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

@Composable
internal fun EvaluationStatusBadge(evaluationStatus: String) {
    val isEvaluated = isEvaluatedStatus(evaluationStatus)
    val text = stringResource(
        if (isEvaluated) {
            R.string.evaluation_status_evaluated
        } else {
            R.string.evaluation_status_not_evaluated
        },
    )
    val backgroundColor = if (isEvaluated) Success else MediumGray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}

internal fun isEvaluatedStatus(evaluationStatus: String): Boolean =
    evaluationStatus.equals("EVALUATED", ignoreCase = true)

private fun formatScore(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
