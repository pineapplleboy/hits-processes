package com.example.googleclass.feature.peerreview.presentation.list

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.googleclass.common.presentation.components.FileChip
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.feature.peerreview.domain.model.AvailableWork
import com.example.googleclass.feature.peerreview.domain.model.PeerEvaluation
import com.example.googleclass.feature.peerreview.domain.model.UnavailableReason
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PeerReviewListScreen(
    courseId: String,
    postId: String,
    onNavigateBack: () -> Unit,
    onOpenEvaluation: (evaluationId: String) -> Unit,
    refreshSignal: Boolean = false,
    onRefreshSignalConsumed: () -> Unit = {},
) {
    val viewModel: PeerReviewListViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.onEvent(PeerReviewListUiEvent.Refresh)
            onRefreshSignalConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                PeerReviewListUiEffect.NavigateBack -> onNavigateBack()
                is PeerReviewListUiEffect.NavigateToEvaluation -> onOpenEvaluation(effect.evaluationId)
                is PeerReviewListUiEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PeerReviewListContent(
        state = uiState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PeerReviewListContent(
    state: PeerReviewListUiState,
    onEvent: (PeerReviewListUiEvent) -> Unit,
) {
    val isRefreshing = (state as? PeerReviewListUiState.Content)?.isRefreshing == true
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { onEvent(PeerReviewListUiEvent.Refresh) },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.peer_review_list_title),
                onNavigateBack = { onEvent(PeerReviewListUiEvent.NavigateBack) },
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
                is PeerReviewListUiState.Loading -> LoadingState()

                is PeerReviewListUiState.Error -> EmptyState(message = state.message)

                is PeerReviewListUiState.Content -> {
                    if (state.assigned.isEmpty() && state.available.isEmpty()) {
                        EmptyState(message = stringResource(R.string.peer_review_empty))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (state.assigned.isNotEmpty()) {
                                item {
                                    SectionTitle(stringResource(R.string.peer_review_assigned_section))
                                }
                                items(state.assigned, key = { it.id }) { evaluation ->
                                    AssignedWorkCard(
                                        evaluation = evaluation,
                                        onClick = {
                                            onEvent(PeerReviewListUiEvent.OpenEvaluation(evaluation.id))
                                        },
                                    )
                                }
                            }

                            if (state.available.isNotEmpty()) {
                                item {
                                    SectionTitle(stringResource(R.string.peer_review_available_section))
                                }
                                items(state.available, key = { it.taskAnswerId }) { work ->
                                    AvailableWorkCard(
                                        work = work,
                                        isSelecting = state.selectingTaskAnswerId == work.taskAnswerId,
                                        onSelect = {
                                            onEvent(PeerReviewListUiEvent.SelectWork(work.taskAnswerId))
                                        },
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun AssignedWorkCard(
    evaluation: PeerEvaluation,
    onClick: () -> Unit,
) {
    InfoCard(onClick = onClick) {
        Text(
            text = evaluation.studentName ?: stringResource(R.string.peer_review_anonymous_student),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (evaluation.score != null) {
                stringResource(R.string.peer_review_evaluated)
            } else {
                stringResource(R.string.peer_review_not_evaluated)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (evaluation.files.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            evaluation.files.forEach { file ->
                FileChip(fileName = file.fileName ?: "Файл")
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun AvailableWorkCard(
    work: AvailableWork,
    isSelecting: Boolean,
    onSelect: () -> Unit,
) {
    InfoCard {
        Text(
            text = work.studentName ?: stringResource(R.string.peer_review_anonymous_student),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (work.files.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            work.files.forEach { file ->
                FileChip(fileName = file.fileName ?: "Файл")
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (work.canAppraise) {
            Button(
                onClick = onSelect,
                enabled = !isSelecting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSelecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.peer_review_take))
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = work.unavailableReason.toMessage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun UnavailableReason?.toMessage(): String = when (this) {
    UnavailableReason.TASK_DEADLINE_HAS_NOT_PASSED -> stringResource(R.string.peer_reason_task_deadline)
    UnavailableReason.APPRAISER_DEADLINE_HAS_PASSED -> stringResource(R.string.peer_reason_appraiser_deadline)
    UnavailableReason.ANSWER_IS_NOT_SUBMITTED -> stringResource(R.string.peer_reason_not_submitted)
    UnavailableReason.OWN_ANSWER -> stringResource(R.string.peer_reason_own)
    UnavailableReason.ALREADY_SELECTED -> stringResource(R.string.peer_reason_already_selected)
    UnavailableReason.APPRAISING_LIMIT_REACHED -> stringResource(R.string.peer_reason_limit)
    UnavailableReason.RECIPROCAL_APPRAISING -> stringResource(R.string.peer_reason_reciprocal)
    UnavailableReason.UNKNOWN, null -> stringResource(R.string.peer_reason_unknown)
}
