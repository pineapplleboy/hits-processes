package com.example.googleclass.feature.courses.presentation

data class CourseUiItem(
    val id: String,
    val name: String,
    val subject: String,
    val role: String,
    val code: String? = null,
)

enum class TaskStatus {
    /** Сдано вовремя (COMPLETED) */
    SUBMITTED,
    /** Сдано с опозданием (COMPETED_AFTER_DEADLINE) */
    SUBMITTED_LATE,
    /** Не сдано / просрочено (NOT_COMPLETED) */
    OVERDUE,
    /** Ещё не начато (NEW) */
    NEW,
}

data class TaskUiItem(
    val id: String,
    val postId: String? = null,
    val courseId: String? = null,
    val title: String,
    val status: TaskStatus,
    val score: String?,
    val maxScore: String?,
    /** Дедлайн из задания (придёт когда API добавит поле) */
    val deadline: String? = null,
    /** Дата сдачи из TaskAnswerModel.submittedAt */
    val submittedAt: String? = null,
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
    val courseId: String? = null,
    val isCreating: Boolean = false,
    val error: String? = null,
)

data class JoinCourseDialogState(
    val code: String = "",
    val isJoining: Boolean = false,
    val error: String? = null,
)