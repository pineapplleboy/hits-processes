package com.example.googleclass.feature.taskdetail.presentation

import androidx.lifecycle.ViewModel
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskDetailViewModel : ViewModel() {

    private val _state: MutableStateFlow<TaskDetailScreenState> =
        MutableStateFlow(TaskDetailScreenState.Loading)
    val state: StateFlow<TaskDetailScreenState> = _state.asStateFlow()

    init {
        loadStudentMockData()
    }

    fun onStudentTabSelected(tab: StudentTab) {
        val currentState = _state.value
        if (currentState is TaskDetailScreenState.StudentView) {
            _state.value = currentState.copy(selectedTab = tab)
        }
    }

    fun onTeacherTabSelected(tab: TeacherTab) {
        val currentState = _state.value
        if (currentState is TaskDetailScreenState.TeacherView) {
            _state.value = currentState.copy(selectedTab = tab)
        }
    }

    fun onCommentInputChange(text: String) {
        val currentState = _state.value
        when (currentState) {
            is TaskDetailScreenState.StudentView -> {
                _state.value = currentState.copy(commentInput = text)
            }

            is TaskDetailScreenState.TeacherView -> {
                _state.value = currentState.copy(commentInput = text)
            }

            else -> Unit
        }
    }

    fun onSendComment() {
        val currentState = _state.value
        when (currentState) {
            is TaskDetailScreenState.StudentView -> {
                if (currentState.commentInput.isBlank()) return
                _state.value = currentState.copy(commentInput = "")
            }

            is TaskDetailScreenState.TeacherView -> {
                if (currentState.commentInput.isBlank()) return
                _state.value = currentState.copy(commentInput = "")
            }

            else -> Unit
        }
    }

    fun onOpenStudentChat(studentId: String) {
        // Navigation to student chat
    }

    fun loadStudentMockData() {
        _state.value = TaskDetailScreenState.StudentView(
            task = TaskDetail(
                id = "1",
                title = "Задание 1: Основы синтаксиса",
                authorName = "Иванов Иван Иванович",
                createdAt = "17 января, 14:00",
                description = "Напишите программу, которая выводит \"Hello, World!\" и вычисляет сумму чисел от 1 до 100.",
                deadline = "20 февраля, 23:59",
                maxScore = 100,
            ),
            submission = Submission(
                submittedAt = "18 февраля, 15:30",
                files = listOf("solution1.py"),
                score = 95,
                maxScore = 100,
                isNewGrade = true,
            ),
            publicComments = emptyList(),
            privateComments = listOf(
                Comment(
                    id = "1",
                    authorName = "Иванов Иван Иванович",
                    text = "Отличная работа! Немного улучшил бы структуру кода.",
                    createdAt = "19 февраля, 10:00",
                ),
                Comment(
                    id = "2",
                    authorName = "Сидоров Алексей",
                    text = "Спасибо за обратную связь!",
                    createdAt = "19 февраля, 11:00",
                ),
            ),
            commentInput = "",
            selectedTab = StudentTab.PUBLIC_COMMENTS,
        )
    }

    fun loadTeacherMockData() {
        _state.value = TaskDetailScreenState.TeacherView(
            task = TaskDetail(
                id = "2",
                title = "Задание 2: Работа со списками",
                authorName = "Петрова Мария Сергеевна",
                createdAt = "1 февраля, 10:00",
                description = "Реализуйте функции для работы со списками: сортировка, поиск элемента, удаление дубликатов.",
                deadline = "25 февраля, 23:59",
                maxScore = 100,
            ),
            publicComments = emptyList(),
            students = listOf(
                StudentSubmissionInfo(
                    studentId = "1",
                    studentName = "Сидоров Алексей",
                    score = null,
                    maxScore = 100,
                    status = SubmissionStatus.OVERDUE,
                ),
                StudentSubmissionInfo(
                    studentId = "2",
                    studentName = "Козлова Анна",
                    score = null,
                    maxScore = 100,
                    status = SubmissionStatus.OVERDUE,
                ),
                StudentSubmissionInfo(
                    studentId = "3",
                    studentName = "Смирнов Дмитрий",
                    score = null,
                    maxScore = 100,
                    status = SubmissionStatus.OVERDUE,
                ),
            ),
            commentInput = "",
            selectedTab = TeacherTab.PUBLIC_COMMENTS,
        )
    }
}
