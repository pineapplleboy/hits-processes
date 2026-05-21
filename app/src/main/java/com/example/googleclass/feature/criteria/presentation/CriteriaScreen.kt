package com.example.googleclass.feature.criteria.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.common.presentation.theme.SecondaryText
import com.example.googleclass.feature.criteria.domain.model.EvaluationCriterion
import com.example.googleclass.feature.criteria.domain.model.EvaluationFunction
import com.example.googleclass.feature.criteria.domain.model.usesPassFailScale
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CriteriaScreen(
    courseId: String,
    postId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: CriteriaViewModel = koinViewModel(
        parameters = { parametersOf(courseId, postId) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CriteriaUiEffect.NavigateBack -> onNavigateBack()
                is CriteriaUiEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    CriteriaContent(
        state = uiState,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CriteriaContent(
    state: CriteriaUiState,
    onEvent: (CriteriaUiEvent) -> Unit,
) {
    val contentState = state as? CriteriaUiState.Content
    var descriptionPreview by remember { mutableStateOf<EvaluationCriterion?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.criteria_screen_title),
                onNavigateBack = { onEvent(CriteriaUiEvent.NavigateBack) },
            )
        },
        floatingActionButton = {
            if (contentState != null) {
                FloatingActionButton(
                    onClick = { onEvent(CriteriaUiEvent.AddCriterion) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.criteria_add),
                    )
                }
            }
        },
    ) { padding ->
        when (state) {
            CriteriaUiState.Loading -> LoadingState()
            is CriteriaUiState.Content -> {
                CriteriaList(
                    state = state,
                    onEvent = onEvent,
                    onDescriptionClick = { descriptionPreview = it },
                    modifier = Modifier.padding(padding),
                )

                state.editor?.let { editor ->
                    CriterionEditorSheet(
                        state = editor,
                        onEvent = onEvent,
                    )
                }

                state.pendingDelete?.let { criterion ->
                    DeleteCriterionDialog(
                        criterion = criterion,
                        onDismiss = { onEvent(CriteriaUiEvent.DismissDelete) },
                        onConfirm = { onEvent(CriteriaUiEvent.ConfirmDelete) },
                    )
                }

                descriptionPreview
                    ?.takeIf { it.description.takeMeaningfulDescription() != null }
                    ?.let { criterion ->
                        CriterionDescriptionDialog(
                            criterionName = criterion.name,
                            description = criterion.description.takeMeaningfulDescription().orEmpty(),
                            onDismiss = { descriptionPreview = null },
                        )
                    }
            }
        }
    }
}

@Composable
private fun CriteriaList(
    state: CriteriaUiState.Content,
    onEvent: (CriteriaUiEvent) -> Unit,
    onDescriptionClick: (EvaluationCriterion) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.criteria.isEmpty() && !state.isRefreshing) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.criteria_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.criteria_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
                Button(
                    onClick = { onEvent(CriteriaUiEvent.AddCriterion) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(text = stringResource(R.string.criteria_add_first))
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.criteria_count_format, state.criteria.size),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
            )
        }

        itemsIndexed(
            items = state.criteria,
            key = { _, criterion -> criterion.id },
        ) { index, criterion ->
            CriterionCard(
                index = index + 1,
                criterion = criterion,
                onDescriptionClick = { onDescriptionClick(criterion) },
                onEditClick = { onEvent(CriteriaUiEvent.EditCriterion(criterion.id)) },
                onDeleteClick = { onEvent(CriteriaUiEvent.RequestDeleteCriterion(criterion.id)) },
            )
        }
    }
}

@Composable
private fun CriterionCard(
    index: Int,
    criterion: EvaluationCriterion,
    onDescriptionClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = criterion.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (criterion.description.takeMeaningfulDescription() != null) {
                            CriterionDescriptionButton(onClick = onDescriptionClick)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = criterion.toSummaryLine(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.criteria_edit),
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.criteria_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CriterionEditorSheet(
    state: CriterionEditorState,
    onEvent: (CriteriaUiEvent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(CriteriaUiEvent.DismissEditor) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (state.mode == CriterionEditorMode.CREATE) {
                                R.string.criteria_editor_create_title
                            } else {
                                R.string.criteria_editor_edit_title
                            },
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.criteria_editor_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                    )
                }

                TextButton(onClick = { onEvent(CriteriaUiEvent.DismissEditor) }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(CriteriaUiEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.criteria_name_label)) },
                placeholder = { Text(stringResource(R.string.criteria_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(CriteriaUiEvent.DescriptionChanged(it)) },
                label = { Text(stringResource(R.string.criteria_description_label)) },
                placeholder = { Text(stringResource(R.string.criteria_description_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            CriterionEvaluationFunctionSelector(
                selectedFunction = state.evaluationFunction,
                onFunctionSelected = { onEvent(CriteriaUiEvent.EvaluationFunctionChanged(it)) },
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(CriteriaUiEvent.PassFailChanged(!state.isPassFail)) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.criteria_pass_fail_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.criteria_pass_fail_score_helper),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                        )
                    }

                    Switch(
                        checked = state.isPassFail,
                        onCheckedChange = { onEvent(CriteriaUiEvent.PassFailChanged(it)) },
                    )
                }
            }

            if (!state.isPassFail) {
                OutlinedTextField(
                    value = state.minScore,
                    onValueChange = { onEvent(CriteriaUiEvent.MinScoreChanged(it)) },
                    label = { Text(stringResource(R.string.criteria_min_score_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            } else {
                Text(
                    text = stringResource(R.string.criteria_pass_fail_min_score_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            }

            OutlinedTextField(
                value = if (state.isPassFail) "1" else state.maxScore,
                onValueChange = {
                    if (!state.isPassFail) {
                        onEvent(CriteriaUiEvent.MaxScoreChanged(it))
                    }
                },
                enabled = !state.isPassFail,
                label = { Text(stringResource(R.string.max_score_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            if (state.allowsMultiplier) {
                OutlinedTextField(
                    value = state.multiplier,
                    onValueChange = { onEvent(CriteriaUiEvent.MultiplierChanged(it)) },
                    label = { Text(stringResource(R.string.criteria_multiplier_label)) },
                    placeholder = { Text(stringResource(R.string.criteria_multiplier_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                Text(
                    text = stringResource(R.string.criteria_multiplier_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                )
            }

            Button(
                onClick = { onEvent(CriteriaUiEvent.SaveCriterion) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
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
                        text = stringResource(
                            if (state.mode == CriterionEditorMode.CREATE) {
                                R.string.criteria_editor_create_action
                            } else {
                                R.string.criteria_editor_save_action
                            },
                        ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CriterionEvaluationFunctionSelector(
    selectedFunction: EvaluationFunction,
    onFunctionSelected: (EvaluationFunction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.criteria_evaluation_function_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EvaluationFunction.entries.forEach { function ->
                FilterChip(
                    selected = function == selectedFunction,
                    onClick = { onFunctionSelected(function) },
                    label = {
                        Text(
                            text = when (function) {
                                EvaluationFunction.SUM -> stringResource(R.string.task_eval_function_sum)
                                EvaluationFunction.MULTIPLY -> stringResource(R.string.task_eval_function_multiply)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DeleteCriterionDialog(
    criterion: EvaluationCriterion,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.criteria_delete_dialog_title)) },
        text = {
            Text(
                text = stringResource(R.string.criteria_delete_dialog_message, criterion.name),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.criteria_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

private fun EvaluationCriterion.toSummaryLine(): String {
    if (usesPassFailScale) return "(+/-)"

    val multiplierSuffix = multiplier?.let { " x${formatDecimal(it)}" }.orEmpty()
    return "${formatDecimal(minScore)}-${formatDecimal(maxScore)}$multiplierSuffix"
}

private fun formatDecimal(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
