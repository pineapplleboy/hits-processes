package com.example.googleclass.feature.criteria.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun CriteriaEvaluationScreen(
    courseId: String,
    postId: String,
    taskAnswerId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: CriteriaEvaluationViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId, taskAnswerId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CriteriaEvaluationUiEffect.NavigateBack -> onNavigateBack()
                is CriteriaEvaluationUiEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when (val state = uiState) {
        CriteriaEvaluationUiState.Loading -> LoadingState()
        is CriteriaEvaluationUiState.Content -> CriteriaEvaluationContent(
            state = state,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun CriteriaEvaluationContent(
    state: CriteriaEvaluationUiState.Content,
    onEvent: (CriteriaEvaluationUiEvent) -> Unit,
) {
    var descriptionDialogState by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.criteria_evaluation_screen_title),
                onNavigateBack = { onEvent(CriteriaEvaluationUiEvent.NavigateBack) },
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { onEvent(CriteriaEvaluationUiEvent.Save) },
                    enabled = !state.isSaving && state.criteria.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF090A1F),
                        contentColor = Color.White,
                    ),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SummaryHeaderCard(
                    taskTitle = state.taskTitle,
                    studentName = state.studentName,
                )
            }

            item {
                SubmissionInfoCard(
                    files = state.files,
                    comments = state.comments,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.criteria_evaluation_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText,
                )
            }

            itemsIndexed(
                items = state.criteria,
                key = { _, criterion -> criterion.markCriteriaId },
            ) { index, criterion ->
                CriteriaEvaluationCard(
                    index = index + 1,
                    criterion = criterion,
                    onDescriptionClick = {
                        criterion.description.takeMeaningfulDescription()?.let { description ->
                            descriptionDialogState = criterion.name to description
                        }
                    },
                    onScoreChanged = { value ->
                        onEvent(
                            CriteriaEvaluationUiEvent.ScoreChanged(
                                markCriteriaId = criterion.markCriteriaId,
                                value = value,
                            ),
                        )
                    },
                )
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
private fun SummaryHeaderCard(
    taskTitle: String,
    studentName: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3A82FF), PrimaryBlue),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
    }
}

@Composable
private fun SubmissionInfoCard(
    files: List<TaskAnswerFile>,
    comments: List<Comment>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.criteria_evaluation_files_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (files.isEmpty()) {
                Text(
                    text = stringResource(R.string.criteria_evaluation_no_files),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    files.forEach { file ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                IconMarker()
                                Text(
                                    text = file.fileName ?: stringResource(R.string.file_name_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            if (comments.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.criteria_evaluation_comments_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                    )
                    comments.forEach { comment ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CriteriaEvaluationCard(
    index: Int,
    criterion: CriteriaEvaluationFieldState,
    onDescriptionClick: () -> Unit,
    onScoreChanged: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = criterion.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            if (criterion.description.takeMeaningfulDescription() != null) {
                                CriterionDescriptionButton(onClick = onDescriptionClick)
                            }
                        }
                        criterion.multiplier?.let {
                            Text(
                                text = stringResource(
                                    R.string.criteria_evaluation_multiplier_format,
                                    formatScore(it),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF090A1F),
                ) {
                    Text(
                        text = stringResource(
                            R.string.criteria_evaluation_range_chip,
                            formatScore(criterion.minScore),
                            formatScore(criterion.maxScore),
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = criterion.input,
                    onValueChange = onScoreChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Text(
                    text = stringResource(
                        R.string.criteria_evaluation_max_suffix,
                        formatScore(criterion.maxScore),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MediumGray,
                )
            }
        }
    }
}

@Composable
private fun IconMarker() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(PrimaryBlue.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun formatScore(value: Float): String =
    if (value == value.roundToInt().toFloat()) value.roundToInt().toString() else value.toString()
