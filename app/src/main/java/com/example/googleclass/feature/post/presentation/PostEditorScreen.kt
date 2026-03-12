package com.example.googleclass.feature.post.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.theme.PrimaryBlue
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.taskdetail.presentation.rememberFilePicker
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PostEditorScreen(
    mode: PostEditorMode,
    onNavigateBack: () -> Unit,
    onNavigateToCourseFeed: (courseId: String) -> Unit = {},
) {
    val viewModel: PostEditorViewModel = koinViewModel(
        parameters = { parametersOf(mode) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePicker = rememberFilePicker(
        onFilePicked = { uri, displayName ->
            viewModel.onEvent(PostEditorUiEvent.FileAttached(uri, displayName))
        },
    )

    val uiEffect by viewModel.uiEffect.collectAsStateWithLifecycle(PostEditorUiEffect.None)
    LaunchedEffect(uiEffect) {
        when (val e = uiEffect) {
            is PostEditorUiEffect.NavigateBack -> {
                viewModel.consumeEffect()
                onNavigateBack()
            }
            is PostEditorUiEffect.ShowError -> {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeEffect()
            }
            is PostEditorUiEffect.NavigateToCourseFeed -> {
                viewModel.consumeEffect()
                Toast.makeText(
                    context,
                    context.getString(R.string.post_saved),
                    Toast.LENGTH_SHORT,
                ).show()
                onNavigateToCourseFeed(e.courseId)
            }
            PostEditorUiEffect.None -> {}
        }
    }

    PostEditorContent(
        state = uiState,
        onEvent = viewModel::onEvent,
        onPickFromDocuments = { filePicker.launchDocuments() },
        onPickFromGallery = { filePicker.launchGallery() },
    )
}

@Composable
private fun PostEditorContent(
    state: PostEditorUiState,
    onEvent: (PostEditorUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit = {},
    onPickFromGallery: () -> Unit = {},
) {
    val isCreateMode = state is PostEditorUiState.Content &&
        state.mode is PostEditorMode.Create

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(
                    if (isCreateMode) R.string.create_post_title
                    else R.string.edit_post_title
                ),
                onNavigateBack = { onEvent(PostEditorUiEvent.NavigateBack) },
            )
        },
    ) { padding ->
        when (state) {
            is PostEditorUiState.Loading -> LoadingState()

            is PostEditorUiState.Content -> PostEditorForm(
                state = state,
                onEvent = onEvent,
                onPickFromDocuments = onPickFromDocuments,
                onPickFromGallery = onPickFromGallery,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PostEditorForm(
    state: PostEditorUiState.Content,
    onEvent: (PostEditorUiEvent) -> Unit,
    onPickFromDocuments: () -> Unit,
    onPickFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.text,
            onValueChange = { onEvent(PostEditorUiEvent.TextChanged(it)) },
            label = { Text(stringResource(R.string.post_text_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        if (state.isPostTypeEditable) {
            PostTypeSelector(
                selectedType = state.selectedPostType,
                onTypeSelected = { onEvent(PostEditorUiEvent.PostTypeSelected(it)) },
                enabled = true,
            )
        }

        if (state.selectedPostType == PostType.TASK && state.isPostTypeEditable) {
            OutlinedTextField(
                value = state.maxScore,
                onValueChange = { onEvent(PostEditorUiEvent.MaxScoreChanged(it)) },
                label = { Text(stringResource(R.string.max_score_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            DeadlinePicker(
                value = state.deadline,
                onValueChange = { onEvent(PostEditorUiEvent.DeadlineChanged(it)) },
            )
        }

        PostAttachmentSection(
            attachedFiles = state.attachedFiles,
            existingAttachments = state.existingAttachments,
            onEvent = onEvent,
            onPickFromDocuments = onPickFromDocuments,
            onPickFromGallery = onPickFromGallery,
        )

        val isFormValid = state.text.isNotBlank() && !state.isSaving

        Button(
            onClick = { onEvent(PostEditorUiEvent.Save) },
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White,
            ),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(
                        if (state.mode is PostEditorMode.Create) R.string.create
                        else R.string.save
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePicker(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var pendingDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    val initialMillis = try {
        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            .parse(value.trim())?.time
    } catch (_: Exception) {
        null
    } ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
    )

    val (initialHour, initialMinute) = try {
        val cal = java.util.Calendar.getInstance()
        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            .parse(value.trim())?.let { cal.time = it }
        cal.get(java.util.Calendar.HOUR_OF_DAY) to cal.get(java.util.Calendar.MINUTE)
    } catch (_: Exception) {
        23 to 59
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.deadline_hint)) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    painter = painterResource(R.drawable.clock),
                    contentDescription = stringResource(R.string.deadline_hint),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showDatePicker = true },
            ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { ms ->
                            pendingDateMillis = ms
                            showDatePicker = false
                            showTimePicker = true
                        }
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false; pendingDateMillis = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dateMs = pendingDateMillis ?: System.currentTimeMillis()
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = dateMs
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                        }
                        onValueChange(PostEditorViewModel.formatDeadlineForDisplay(cal.timeInMillis))
                        showTimePicker = false
                        pendingDateMillis = null
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.deadline_hint)) },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}
