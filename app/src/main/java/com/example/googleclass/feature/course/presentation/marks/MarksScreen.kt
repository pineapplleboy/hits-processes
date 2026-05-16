package com.example.googleclass.feature.course.presentation.marks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.components.ClassroomTopAppBar
import com.example.googleclass.common.presentation.components.ContentDivider
import com.example.googleclass.common.presentation.components.EmptyState
import com.example.googleclass.common.presentation.components.InfoCard
import com.example.googleclass.common.presentation.components.UserAvatar
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
                title = "Оценки",
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
                        message = "Пока нет студентов",
                        modifier = Modifier.padding(paddingValues),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                text = "Студенты (${state.students.size})",
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

@Composable
private fun StudentMarkCard(
    student: StudentMarkItem,
) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(name = student.name, size = 40)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                    StudentMarkItem("1", "Иванов Иван", "ivanov@example.com", "STUDENT"),
                    StudentMarkItem("2", "Петрова Мария", "petrova@example.com", "STUDENT"),
                    StudentMarkItem("3", "Сидоров Алексей", "sidorov@example.com", "STUDENT"),
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
