package com.example.googleclass.feature.peerreview.presentation.evaluate

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.components.FileChip
import com.example.googleclass.common.presentation.components.InfoCard
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun PeerEvaluationScreen(
    courseId: String,
    postId: String,
    evaluationId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: PeerEvaluationViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId, evaluationId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PeerEvaluationUiEffect.NavigateBack -> onNavigateBack()
                is PeerEvaluationUiEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.peer_eval_title),
                onNavigateBack = { viewModel.onEvent(PeerEvaluationUiEvent.NavigateBack) },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is PeerEvaluationUiState.Loading -> LoadingState()
            is PeerEvaluationUiState.Error -> EmptyState(
                message = state.message,
                modifier = Modifier.padding(padding),
            )
            is PeerEvaluationUiState.Content -> PeerEvaluationContent(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PeerEvaluationContent(
    state: PeerEvaluationUiState.Content,
    onEvent: (PeerEvaluationUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InfoCard {
            Text(
                text = state.studentName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (state.files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.peer_eval_files),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                state.files.forEach { file ->
                    FileChip(fileName = file.fileName ?: "Файл")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (state.usesCriteria) {
            state.criteria.forEach { criterion ->
                InfoCard {
                    Text(
                        text = criterion.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!criterion.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = criterion.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = criterion.input,
                        onValueChange = {
                            onEvent(PeerEvaluationUiEvent.CriterionScoreChanged(criterion.markCriteriaId, it))
                        },
                        label = {
                            Text(
                                "${formatScore(criterion.minScore)} – ${formatScore(criterion.maxScore)}",
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            state.calculatedScore?.let { score ->
                Text(
                    text = stringResource(R.string.peer_eval_total, formatScore(score)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            InfoCard {
                OutlinedTextField(
                    value = state.singleScoreInput,
                    onValueChange = { onEvent(PeerEvaluationUiEvent.SingleScoreChanged(it)) },
                    label = {
                        Text("${stringResource(R.string.peer_eval_score_hint)} (0 – ${formatScore(state.taskMaxScore)})")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(
            onClick = { onEvent(PeerEvaluationUiEvent.Save) },
            enabled = !state.isSaving,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = stringResource(R.string.peer_eval_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun formatScore(value: Float): String =
    if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
