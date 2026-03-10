package com.example.googleclass.feature.taskdetail.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val postId: String,
    private val commentRepository: CommentRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<TaskDetailUiState> =
        MutableStateFlow(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TaskDetailUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect

    init {
        loadTeacherMockData()
        loadPostComments()
    }

    fun onEvent(event: TaskDetailUiEvent) {
        when (event) {
            is TaskDetailUiEvent.NavigateBack -> sendEffect(TaskDetailUiEffect.NavigateBack)
            is TaskDetailUiEvent.SubmitWork -> handleSubmitWork()
            is TaskDetailUiEvent.SendComment -> handleSendComment()
            is TaskDetailUiEvent.FileAttached -> handleFileAttached(event.uri, event.displayName)
            is TaskDetailUiEvent.FileRemoved -> handleFileRemoved(event.uri)
            is TaskDetailUiEvent.CommentInputChanged -> handleCommentInput(event.text)
            is TaskDetailUiEvent.StudentTabSelected -> handleStudentTab(event.tab)
            is TaskDetailUiEvent.TeacherTabSelected -> handleTeacherTab(event.tab)
            is TaskDetailUiEvent.OpenStudentChat -> sendEffect(
                TaskDetailUiEffect.NavigateToStudentChat(
                    taskAnswerId = event.taskAnswerId,
                    studentName = event.studentName,
                    studentUserId = event.studentUserId,
                )
            )
            is TaskDetailUiEvent.DownloadFile -> sendEffect(
                TaskDetailUiEffect.StartFileDownload(event.fileId)
            )
        }
    }

    private fun handleStudentTab(tab: StudentTab) {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView) {
            _uiState.value = state.copy(selectedTab = tab)
        }
    }

    private fun handleTeacherTab(tab: TeacherTab) {
        val state = _uiState.value
        if (state is TaskDetailUiState.TeacherView) {
            _uiState.value = state.copy(selectedTab = tab)
        }
    }

    private fun handleCommentInput(text: String) {
        when (val state = _uiState.value) {
            is TaskDetailUiState.StudentView -> _uiState.value = state.copy(commentInput = text)
            is TaskDetailUiState.TeacherView -> _uiState.value = state.copy(commentInput = text)
            else -> Unit
        }
    }

    private fun handleFileAttached(uri: Uri, displayName: String) {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView && state.submission == null) {
            val newFile = AttachedFile(uri = uri, displayName = displayName)
            _uiState.value = state.copy(
                attachedFiles = state.attachedFiles + newFile,
            )
        }
    }

    private fun handleFileRemoved(uri: Uri) {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView) {
            _uiState.value = state.copy(
                attachedFiles = state.attachedFiles.filter { it.uri != uri },
            )
        }
    }

    private fun handleSendComment() {
        when (val state = _uiState.value) {
            is TaskDetailUiState.StudentView -> {
                if (state.commentInput.isBlank()) return
                val text = state.commentInput
                _uiState.value = state.copy(commentInput = "")
                sendPostComment(text)
            }

            is TaskDetailUiState.TeacherView -> {
                if (state.commentInput.isBlank()) return
                val text = state.commentInput
                _uiState.value = state.copy(commentInput = "")
                sendPostComment(text)
            }

            else -> Unit
        }
    }

    private fun sendPostComment(text: String) {
        viewModelScope.launch {
            commentRepository.createPostComment(postId, text)
                .onSuccess { loadPostComments() }
                .onFailure {
                    sendEffect(
                        TaskDetailUiEffect.ShowError(
                            it.message ?: "Ошибка отправки комментария"
                        )
                    )
                }
        }
    }

    private fun loadPostComments() {
        viewModelScope.launch {
            commentRepository.getPostComments(postId)
                .onSuccess { comments ->
                    when (val state = _uiState.value) {
                        is TaskDetailUiState.StudentView -> {
                            _uiState.value = state.copy(publicComments = comments)
                        }
                        is TaskDetailUiState.TeacherView -> {
                            _uiState.value = state.copy(publicComments = comments)
                        }
                        else -> Unit
                    }
                }
                .onFailure {
                    sendEffect(
                        TaskDetailUiEffect.ShowError(
                            it.message ?: "Ошибка загрузки комментариев"
                        )
                    )
                }
        }
    }

    private fun handleSubmitWork() {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView && state.attachedFiles.isNotEmpty()) {
            val uris = state.attachedFiles.map { it.uri }
            sendEffect(TaskDetailUiEffect.StartFileUpload(uris))
        }
    }

    private fun sendEffect(effect: TaskDetailUiEffect) {
        viewModelScope.launch {
            _uiEffect.tryEmit(effect)
        }
    }

    fun loadStudentMockData() {
        _uiState.value = TaskDetailUiState.StudentView(
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
            attachedFiles = emptyList(),
            commentInput = "",
            selectedTab = StudentTab.PUBLIC_COMMENTS,
        )
    }

    fun loadStudentNotSubmittedMockData() {
        _uiState.value = TaskDetailUiState.StudentView(
            task = TaskDetail(
                id = "2",
                title = "Задание 2: Работа со списками",
                authorName = "Петрова Мария Сергеевна",
                createdAt = "1 февраля, 10:00",
                description = "Реализуйте функции для работы со списками: сортировка, поиск элемента, удаление дубликатов.",
                deadline = "25 февраля, 23:59",
                maxScore = 100,
            ),
            submission = null,
            publicComments = emptyList(),
            privateComments = emptyList(),
            attachedFiles = emptyList(),
            commentInput = "",
            selectedTab = StudentTab.PUBLIC_COMMENTS,
        )
    }

    fun loadTeacherMockData() {
        _uiState.value = TaskDetailUiState.TeacherView(
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
