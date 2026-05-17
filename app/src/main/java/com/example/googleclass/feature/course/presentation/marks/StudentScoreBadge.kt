package com.example.googleclass.feature.course.presentation.marks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.googleclass.R

@Composable
fun StudentScoreBadge(
    score: Float?,
    isPassFail: Boolean,
) {
    if (score == null) return

    if (isPassFail) {
        val passed = score.toInt() == 1
        val backgroundColor = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336)
        val text = if (passed) {
            stringResource(R.string.marks_passed)
        } else {
            stringResource(R.string.marks_not_passed)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    } else {
        val scoreColor = when {
            score < 3f -> Color(0xFFF44336)
            score < 4f -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }
        Text(
            text = if (score % 1f == 0f) score.toInt().toString() else score.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = scoreColor,
        )
    }
}
