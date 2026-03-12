package com.example.googleclass.feature.taskdetail.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.feature.taskdetail.domain.model.Submission

@Composable
internal fun SubmissionCard(
    submission: Submission,
    showUnsubmit: Boolean = false,
    onUnsubmit: () -> Unit = {},
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

            if (showUnsubmit) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.TextButton(
                    onClick = onUnsubmit,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.unsubmit_work),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GradeBadge(
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
