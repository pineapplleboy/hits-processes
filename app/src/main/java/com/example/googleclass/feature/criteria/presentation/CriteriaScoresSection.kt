package com.example.googleclass.feature.criteria.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CriteriaScoresSection(
    taskAnswerId: String,
    isSelfAssessment: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewModel: CriteriaScoresViewModel = koinViewModel(
        parameters = { parametersOf(taskAnswerId, isSelfAssessment) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(CriteriaScoresUiEffect.None)
    val context = LocalContext.current
    var descriptionDialogState by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(uiEffect) {
        when (val effect = uiEffect) {
            CriteriaScoresUiEffect.None -> Unit
            is CriteriaScoresUiEffect.ShowMessage -> {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeEffect()
            }
        }
    }

    when (val state = uiState) {
        CriteriaScoresUiState.Loading -> LoadingState()
        is CriteriaScoresUiState.Content -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.scores.isEmpty()) {
                    Text(
                        text = stringResource(R.string.criteria_scores_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (isSelfAssessment) {
                                R.string.criteria_scores_self_assessment_title
                            } else {
                                R.string.criteria_scores_teacher_title
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    state.scores.forEach { scoreState ->
                        CriteriaScoreCard(
                            state = scoreState,
                            onDescriptionClick = {
                                scoreState.criterion.description.takeMeaningfulDescription()?.let { description ->
                                    descriptionDialogState = scoreState.criterion.name to description
                                }
                            },
                            onScoreChanged = { value ->
                                viewModel.onEvent(
                                    CriteriaScoresUiEvent.ScoreChanged(
                                        markCriteriaId = scoreState.criterion.markCriteriaId,
                                        value = value,
                                    ),
                                )
                            },
                            onSaveClick = {
                                viewModel.onEvent(
                                    CriteriaScoresUiEvent.SaveScore(scoreState.criterion.markCriteriaId),
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    descriptionDialogState?.let { (criterionName, description) ->
        CriterionDescriptionDialog(
            criterionName = criterionName,
            description = description,
            onDismiss = { descriptionDialogState = null },
        )
    }
}

@Composable
private fun CriteriaScoreCard(
    state: CriteriaScoreFieldState,
    onDescriptionClick: () -> Unit,
    onScoreChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
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
                    text = state.criterion.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (state.criterion.description.takeMeaningfulDescription() != null) {
                    CriterionDescriptionButton(onClick = onDescriptionClick)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScoreInfoChip(
                    text = stringResource(
                        R.string.criteria_score_range_format,
                        formatDecimal(state.criterion.minScore),
                        formatDecimal(state.criterion.maxScore),
                    ),
                )

                state.criterion.multiplier?.let {
                    ScoreInfoChip(text = "x${formatDecimal(it)}")
                }
            }

            OutlinedTextField(
                value = state.input,
                onValueChange = onScoreChanged,
                label = { Text(stringResource(R.string.criteria_score_input_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            Button(
                onClick = onSaveClick,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
private fun ScoreInfoChip(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

private fun formatDecimal(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
