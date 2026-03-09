package com.example.googleclass.feature.courses.presentation

data class CourseUiItem(
    val id: String,
    val name: String,
    val subject: String,
    val role: String,
    val code: String,
)

enum class TaskStatus {
    SUBMITTED,
    OVERDUE,
}

data class TaskUiItem(
    val id: String,
    val title: String,
    val status: TaskStatus,
    val score: String?,
    val maxScore: String?,
    val deadline: String,
)

sealed interface CoursesScreenState {

    data object Loading : CoursesScreenState

    data class Content(
        val courses: List<CourseUiItem>,
        val tasks: List<TaskUiItem>,
        val userName: String,
    ) : CoursesScreenState

    data class Error(val message: String) : CoursesScreenState
}

data class CreateCourseDialogState(
    val name: String = "",
    val description: String = "",
    val isCreating: Boolean = false,
    val error: String? = null,
)