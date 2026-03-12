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
import androidx.compose.foundation.layout.Arrangement
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
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.Success
import com.example.googleclass.common.presentation.theme.WarningOrange
import com.example.googleclass.feature.taskdetail.domain.model.Submission

private fun statusToBadgeColor(statusText: String?): Color {
    if (statusText.isNullOrBlank()) return Success
    return when {
        statusText.contains("опозданием", ignoreCase = true) -> WarningOrange
        statusText.contains("Не сдано", ignoreCase = true) -> ErrorRed
        statusText.contains("Не начато", ignoreCase = true) -> MediumGray
        else -> Success
    }
}

@Composable
internal fun SubmissionCard(
    submission: Submission,
    statusText: String? = null,
    showUnsubmit: Boolean = false,
    onUnsubmit: () -> Unit = {},
) {
    val badgeColor = statusToBadgeColor(statusText)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.your_work),
                    style = MaterialTheme.typography.labelLarge,
                    color = MediumGray,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = submission.submittedAt,
                        style = MaterialTheme.typography.labelMedium,
                        color = MediumGray,
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "\u2713",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.grade_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MediumGray,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (submission.score != null && submission.score > 0) {
                            GradeBadge(
                                score = submission.score,
                                maxScore = submission.maxScore,
                                isNew = submission.isNewGrade,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.not_graded),
                                style = MaterialTheme.typography.titleMedium,
                                color = MediumGray,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.files_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MediumGray,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        submission.files.forEach { fileName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
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
                        style = MaterialTheme.typography.labelMedium,
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
            .clip(RoundedCornerShape(14.dp))
            .background(Success)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = badgeText,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
