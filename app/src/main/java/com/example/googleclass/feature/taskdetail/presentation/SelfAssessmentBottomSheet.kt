package com.example.googleclass.feature.taskdetail.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.googleclass.feature.criteria.presentation.CriterionDescriptionButton
import com.example.googleclass.feature.criteria.presentation.CriterionDescriptionDialog
import com.example.googleclass.feature.criteria.presentation.SelfAssessmentFieldState
import com.example.googleclass.feature.criteria.presentation.SelfAssessmentUiEffect
import com.example.googleclass.feature.criteria.presentation.SelfAssessmentUiEvent
import com.example.googleclass.feature.criteria.presentation.SelfAssessmentUiState
import com.example.googleclass.feature.criteria.presentation.SelfAssessmentViewModel
import com.example.googleclass.feature.criteria.presentation.takeMeaningfulDescription
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelfAssessmentBottomSheet(
    courseId: String,
    postId: String,
    taskAnswerId: String,
    onDismiss: () -> Unit,
) {
    val viewModel: SelfAssessmentViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId, taskAnswerId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var descriptionDialogState by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SelfAssessmentUiEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.self_assessment_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.self_assessment_sheet_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                SelfAssessmentUiState.Loading -> LoadingState()
                is SelfAssessmentUiState.Content -> {
                    if (state.fields.isEmpty()) {
                        Text(
                            text = stringResource(R.string.criteria_scores_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.fields.forEach { field ->
                                SelfAssessmentCriterionCard(
                                    field = field,
                                    onDescriptionClick = {
                                        field.description.takeMeaningfulDescription()?.let { description ->
                                            descriptionDialogState = field.name to description
                                        }
                                    },
                                    onScoreChanged = { value ->
                                        viewModel.onEvent(
                                            SelfAssessmentUiEvent.ScoreChanged(
                                                markCriteriaId = field.markCriteriaId,
                                                value = value,
                                            )
                                        )
                                    },
                                    onSaveClick = {
                                        viewModel.onEvent(
                                            SelfAssessmentUiEvent.SaveScore(field.markCriteriaId)
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
private fun SelfAssessmentCriterionCard(
    field: SelfAssessmentFieldState,
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
                    text = field.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (field.description.takeMeaningfulDescription() != null) {
                    CriterionDescriptionButton(onClick = onDescriptionClick)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScoreChip(
                    text = stringResource(
                        R.string.criteria_score_range_format,
                        formatScore(field.minScore),
                        formatScore(field.maxScore),
                    ),
                )
                field.multiplier?.let {
                    ScoreChip(text = "x${formatScore(it)}")
                }
            }

            OutlinedTextField(
                value = field.input,
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
                enabled = !field.isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ),
            ) {
                if (field.isSaving) {
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
private fun ScoreChip(text: String) {
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

private fun formatScore(value: Float): String =
    if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
