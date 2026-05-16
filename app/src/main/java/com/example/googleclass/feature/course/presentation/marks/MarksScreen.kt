package com.example.googleclass.feature.course.presentation.marks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MarksScreen(
    courseId: String,
    onNavigateBack: () -> Unit,
) {
    val viewModel: MarksScreenViewModel = koinViewModel(parameters = { parametersOf(courseId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MarksScreenContent(
        state = uiState,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun MarksScreenContent(
    state: MarksScreenState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ClassroomTopAppBar(
                title = stringResource(R.string.marks_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        when (state) {
            is MarksScreenState.Loading -> LoadingState()

            is MarksScreenState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is MarksScreenState.Content -> {
                if (state.students.isEmpty()) {
                    EmptyState(
                        message = stringResource(R.string.marks_empty),
                        modifier = Modifier.padding(paddingValues),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.marks_students_count, state.students.size),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        items(state.students, key = { it.userId }) { student ->
                            StudentMarkCard(student = student)
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Экран оценок")
@Composable
private fun MarksScreenPreview() {
    GoogleClassTheme {
        MarksScreenContent(
            state = MarksScreenState.Content(
                students = listOf(
                    StudentMarkItem("1", "Иванов Иван", "ivanov@example.com", "STUDENT", score = 85f),
                    StudentMarkItem("2", "Петрова Мария", "petrova@example.com", "STUDENT", score = 92f),
                    StudentMarkItem("3", "Сидоров Алексей", "sidorov@example.com", "STUDENT", score = null),
                ),
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Экран оценок — зачёт/незачёт")
@Composable
private fun MarksScreenPassFailPreview() {
    GoogleClassTheme {
        MarksScreenContent(
            state = MarksScreenState.Content(
                students = listOf(
                    StudentMarkItem("1", "Иванов Иван", "ivanov@example.com", "STUDENT", score = 1f, isPassFail = true),
                    StudentMarkItem("2", "Петрова Мария", "petrova@example.com", "STUDENT", score = 0f, isPassFail = true),
                ),
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Экран оценок — пусто")
@Composable
private fun MarksScreenEmptyPreview() {
    GoogleClassTheme {
        MarksScreenContent(
            state = MarksScreenState.Content(students = emptyList()),
            onNavigateBack = {},
        )
    }
}
