package com.example.googleclass.feature.courses.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.ErrorRed
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.SecondaryText
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArchivedCoursesScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit,
    viewModel: ArchivedCoursesViewModel = koinViewModel(),
) {
    ArchivedCoursesScreenContent(
        state = viewModel.state,
        onNavigateBack = onNavigateBack,
        onCourseClick = onCourseClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedCoursesScreenContent(
    state: ArchivedCoursesScreenState,
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_archive),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.archived_courses_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        when (state) {
            is ArchivedCoursesScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ArchivedCoursesScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is ArchivedCoursesScreenState.Content -> {
                if (state.courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.archived_courses_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.courses,
                            key = { it.id },
                        ) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onCourseClick(course.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// region Previews

@Preview(showBackground = true, name = "Архив — пусто")
@Composable
private fun ArchivedCoursesEmptyPreview() {
    GoogleClassTheme {
        ArchivedCoursesScreenContent(
            state = ArchivedCoursesScreenState.Content(courses = emptyList()),
            onNavigateBack = {},
            onCourseClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Архив — контент")
@Composable
private fun ArchivedCoursesContentPreview() {
    GoogleClassTheme {
        ArchivedCoursesScreenContent(
            state = ArchivedCoursesScreenState.Content(
                courses = listOf(
                    CourseUiItem("1", "Старый курс", "Архивный предмет", "Студент"),
                ),
            ),
            onNavigateBack = {},
            onCourseClick = {},
        )
    }
}

// endregion
