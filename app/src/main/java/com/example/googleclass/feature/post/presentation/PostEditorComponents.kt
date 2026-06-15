package com.example.googleclass.feature.post.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.feature.post.data.model.PostCreateDto
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.post.data.model.TaskAnswerAppraisingType
import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PostTypeSelector(
    selectedType: PostType,
    onTypeSelected: (PostType) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.post_type_label_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostType.entries.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { if (enabled) onTypeSelected(type) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = when (type) {
                                PostType.ANNOUNCEMENT -> stringResource(R.string.post_type_announcement)
                                PostType.USEFUL_MATERIAL -> stringResource(R.string.post_type_useful_material)
                                PostType.TASK -> stringResource(R.string.post_type_task)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostAttachmentSection(
    attachedFiles: List<PostAttachedFile>,
    existingAttachments: List<ExistingAttachment>,
    onEvent: (PostEditorUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSourceSheet by remember { mutableStateOf(false) }

    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.pick_source_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showSourceSheet = false
                            onPickFromDocuments()
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.upload),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.pick_source_documents),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(R.string.pick_source_documents_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showSourceSheet = false
                            onPickFromGallery()
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.pick_source_gallery),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(R.string.pick_source_gallery_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray,
                        )
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (existingAttachments.isNotEmpty()) {
            Text(
                text = stringResource(R.string.existing_attachments),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            existingAttachments.forEach { attachment ->
                ExistingAttachmentRow(
                    displayName = attachment.displayName,
                    onRemove = {
                        onEvent(PostEditorUiEvent.ExistingAttachmentRemoved(attachment.id))
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        attachedFiles.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = file.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_file),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEvent(PostEditorUiEvent.FileRemoved(file.uri)) },
                    tint = MediumGray,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp),
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { showSourceSheet = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.upload),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MediumGray,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.attach_file),
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
            )
        }
    }
}

@Composable
internal fun ExistingAttachmentRow(
    displayName: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.upload),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MediumGray,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.remove_file),
            modifier = Modifier
                .size(18.dp)
                .clickable { onRemove() },
            tint = MediumGray,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TaskMarkEvaluationTypeSelector(
    selectedType: TaskMarkEvaluationType,
    onTypeSelected: (TaskMarkEvaluationType) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.task_evaluation_type_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TaskMarkEvaluationType.entries.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { if (enabled) onTypeSelected(type) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = type.toUiLabel(),
                            style = MaterialTheme.typography.bodySmall,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EvaluationFunctionSelector(
    selectedFunction: PostCreateDto.EvaluationFunction,
    onFunctionSelected: (PostCreateDto.EvaluationFunction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.task_eval_function_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PostCreateDto.EvaluationFunction.entries.forEach { func ->
                FilterChip(
                    selected = func == selectedFunction,
                    onClick = { onFunctionSelected(func) },
                    label = {
                        Text(
                            text = when (func) {
                                PostCreateDto.EvaluationFunction.SUM -> stringResource(R.string.task_eval_function_sum)
                                PostCreateDto.EvaluationFunction.MULTIPLY -> stringResource(R.string.task_eval_function_multiply)
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
private fun TaskMarkEvaluationType.toUiLabel(): String = when (this) {
    TaskMarkEvaluationType.TEACHER_DECISION -> stringResource(R.string.task_eval_teacher_decision)
    TaskMarkEvaluationType.TEACHER_DECISION_PASS_FAIL -> stringResource(R.string.task_eval_teacher_decision_pass_fail)
    TaskMarkEvaluationType.SUM -> stringResource(R.string.task_eval_sum)
    TaskMarkEvaluationType.MEAN_VALUE -> stringResource(R.string.task_eval_mean_value)
    TaskMarkEvaluationType.COEFFICIENTS_SUM -> stringResource(R.string.task_eval_coefficients_sum)
    TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE -> stringResource(R.string.task_eval_coefficients_mean_value)
    TaskMarkEvaluationType.SELF_ASSESSMENT -> stringResource(R.string.task_eval_self_assessment)
    TaskMarkEvaluationType.PASS_FAIL -> stringResource(R.string.task_eval_pass_fail)
}

fun TaskMarkEvaluationType.needsMaxScore(): Boolean = when (this) {
    TaskMarkEvaluationType.PASS_FAIL,
    TaskMarkEvaluationType.TEACHER_DECISION_PASS_FAIL -> false
    else -> true
}

fun TaskMarkEvaluationType.needsMinScore(): Boolean = when (this) {
    TaskMarkEvaluationType.TEACHER_DECISION,
    TaskMarkEvaluationType.SUM,
    TaskMarkEvaluationType.MEAN_VALUE,
    TaskMarkEvaluationType.COEFFICIENTS_SUM,
    TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE,
    TaskMarkEvaluationType.SELF_ASSESSMENT -> true
    else -> false
}

fun TaskMarkEvaluationType?.needsMultiplier(): Boolean = when (this) {
    TaskMarkEvaluationType.COEFFICIENTS_SUM,
    TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE -> true
    else -> false
}

fun TaskMarkEvaluationType.needsPassThreshold(): Boolean = when (this) {
    TaskMarkEvaluationType.PASS_FAIL -> true
    else -> false
}

fun TaskMarkEvaluationType?.needsEvaluationFunction(): Boolean = when (this) {
    TaskMarkEvaluationType.COEFFICIENTS_SUM -> true
    else -> false
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PeerReviewSection(
    state: PostEditorScreenState.Content,
    onEvent: (PostEditorUiEvent) -> Unit,
    appraiserDeadlinePicker: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.peer_review_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.peer_review_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray,
                )
            }
            Switch(
                checked = state.peerReviewEnabled,
                onCheckedChange = { onEvent(PostEditorUiEvent.PeerReviewToggled(it)) },
            )
        }

        if (state.peerReviewEnabled) {
            appraiserDeadlinePicker()

            Text(
                text = stringResource(R.string.peer_review_distribution_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskAnswerAppraisingType.entries.forEach { type ->
                    FilterChip(
                        selected = type == state.taskAnswerAppraisingType,
                        onClick = { onEvent(PostEditorUiEvent.TaskAnswerAppraisingTypeSelected(type)) },
                        label = {
                            Text(
                                text = when (type) {
                                    TaskAnswerAppraisingType.CHAIN -> stringResource(R.string.peer_review_type_chain)
                                    TaskAnswerAppraisingType.ANY -> stringResource(R.string.peer_review_type_any)
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

            // Количество работ задаётся только для свободного выбора (ANY);
            // в режиме «по цепочке» (CHAIN) распределение происходит автоматически.
            if (state.taskAnswerAppraisingType == TaskAnswerAppraisingType.ANY) {
                OutlinedTextField(
                    value = state.studentAppraisingNumber,
                    onValueChange = { onEvent(PostEditorUiEvent.StudentAppraisingNumberChanged(it)) },
                    label = { Text(stringResource(R.string.peer_review_count_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            PeerReviewToggleRow(
                title = stringResource(R.string.peer_review_can_see_appraiser),
                checked = state.canSeeAppraiser,
                onCheckedChange = { onEvent(PostEditorUiEvent.CanSeeAppraiserToggled(it)) },
            )
            PeerReviewToggleRow(
                title = stringResource(R.string.peer_review_can_see_appraised),
                checked = state.canSeeAppraised,
                onCheckedChange = { onEvent(PostEditorUiEvent.CanSeeAppraisedToggled(it)) },
            )
        }
    }
}

@Composable
private fun PeerReviewToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
