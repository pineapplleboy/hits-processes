package com.example.googleclass.feature.post.presentation

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
