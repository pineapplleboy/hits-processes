package com.example.googleclass.feature.courses.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.googleclass.R
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.feature.course.domain.model.Course
import org.koin.androidx.compose.koinViewModel

@Composable
fun CoursesScreen(
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    viewModel: CoursesScreenViewModel = koinViewModel(),
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Text(
                text = stringResource(R.string.courses_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            items(
                items = courses,
                key = { it.id }
            ) { course ->
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCourseClick(course.id) }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Список курсов")
@Composable
private fun CoursesScreenPreview() {
    GoogleClassTheme {
        CoursesScreen(
            courses = listOf(
                Course("1", "Математический анализ", emptyList()),
                Course("2", "Линейная алгебра", emptyList()),
            ),
            onCourseClick = { }
        )
    }
}
