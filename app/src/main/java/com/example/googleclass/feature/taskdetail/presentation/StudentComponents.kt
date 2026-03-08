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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus

@Composable
internal fun StudentsList(
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
                onOpenChat = {
                    onEvent(
                        TaskDetailUiEvent.OpenStudentChat(student.studentId, student.studentName)
                    )
                },
            )
        }
    }
}

@Composable
internal fun StudentItem(
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
                    stringResource(R.string.points_suffix)
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
internal fun StatusBadge(status: SubmissionStatus) {
    val (text, backgroundColor) = when (status) {
        SubmissionStatus.SUBMITTED -> stringResource(R.string.status_submitted) to Success
        SubmissionStatus.OVERDUE -> stringResource(R.string.status_overdue) to ErrorRed
        SubmissionStatus.PENDING -> stringResource(R.string.status_pending) to PrimaryBlue
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
