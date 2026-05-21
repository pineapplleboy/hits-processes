package com.example.googleclass.feature.criteria.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.SecondaryText

@Composable
fun CriterionDescriptionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = stringResource(R.string.criteria_description_open),
            tint = SecondaryText,
        )
    }
}

@Composable
fun CriterionDescriptionDialog(
    criterionName: String,
    description: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = criterionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

fun String?.takeMeaningfulDescription(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
