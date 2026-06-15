package com.example.googleclass.feature.peerreview.presentation.teacher

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun AppraisalsScreen(
    taskAnswerId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: AppraisalsViewModel = koinViewModel(
        parameters = { parametersOf(taskAnswerId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AppraisalsUiEffect.NavigateBack -> onNavigateBack()
                is AppraisalsUiEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AppraisalsContent(state = uiState, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AppraisalsContent(
    state: AppraisalsUiState,
    onEvent: (AppraisalsUiEvent) -> Unit,
) {
    val isRefreshing = (state as? AppraisalsUiState.Content)?.isRefreshing == true
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { onEvent(AppraisalsUiEvent.Refresh) },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.appraisals_title),
                onNavigateBack = { onEvent(AppraisalsUiEvent.NavigateBack) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState),
        ) {
            when (state) {
                is AppraisalsUiState.Loading -> LoadingState()
                is AppraisalsUiState.Error -> EmptyState(message = state.message)
                is AppraisalsUiState.Content -> {
                    if (state.appraisers.isEmpty()) {
                        EmptyState(message = stringResource(R.string.appraisals_empty))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.appraisers, key = { it.id }) { appraiser ->
                                AppraiserCard(
                                    appraiser = appraiser,
                                    onOverride = {
                                        onEvent(
                                            AppraisalsUiEvent.OpenOverride(
                                                appraiserId = appraiser.id,
                                                appraiserName = appraiser.appraiserName
                                                    ?: appraiser.appraiserId.orEmpty(),
                                                currentScore = appraiser.score,
                                            )
                                        )
                                    },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }

                    state.overrideDialog?.let { dialog ->
                        OverrideDialog(
                            dialog = dialog,
                            onScoreChange = { onEvent(AppraisalsUiEvent.OverrideScoreChanged(it)) },
                            onConfirm = { onEvent(AppraisalsUiEvent.SubmitOverride) },
                            onDismiss = { onEvent(AppraisalsUiEvent.DismissOverride) },
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AppraiserCard(
    appraiser: PeerEvaluation,
    onOverride: () -> Unit,
) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = appraiser.appraiserName
                    ?: stringResource(R.string.peer_appraiser_anonymous),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = appraiser.score?.let { stringResource(R.string.peer_appraiser_score, formatScore(it)) }
                    ?: stringResource(R.string.peer_appraiser_not_scored),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (appraiser.criteriaScores.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            appraiser.criteriaScores.forEach { criterion ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = criterion.name ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = criterion.score?.let(::formatScore) ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onOverride,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.appraisals_override))
        }
    }
}

@Composable
private fun OverrideDialog(
    dialog: OverrideDialogState,
    onScoreChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.appraisals_override_title)) },
        text = {
            Column {
                Text(
                    text = dialog.appraiserName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dialog.scoreInput,
                    onValueChange = onScoreChange,
                    label = { Text(stringResource(R.string.peer_eval_score_hint)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !dialog.isSaving) {
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

private fun formatScore(value: Float): String =
    if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
