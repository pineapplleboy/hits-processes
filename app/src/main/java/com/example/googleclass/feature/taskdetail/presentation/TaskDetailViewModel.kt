package com.example.googleclass.feature.taskdetail.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.post.data.model.PostModel
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.domain.model.TaskFile
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Calendar
import java.util.Locale

class TaskDetailViewModel(
    private val courseId: String,
    private val postId: String,
    private val userRole: UserRole,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userApi: UserApi,
) : ViewModel() {

    private val _uiState: MutableStateFlow<TaskDetailUiState> =
        MutableStateFlow(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TaskDetailUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect

    init {
        loadPost()
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
            is TaskDetailUiEvent.EditPost -> sendEffect(
                TaskDetailUiEffect.NavigateToEdit(courseId = courseId, postId = postId)
            )
            is TaskDetailUiEvent.DeletePost -> handleDeletePost()
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading

            val profileResult = runCatching { userApi.getMyProfile() }
            val currentUserId = profileResult.getOrNull()
                ?.takeIf { it.isSuccessful }?.body()?.id

            postRepository.getPost(courseId, postId)
                .onSuccess { post ->
                    val task = post.toTaskDetail()
                    val isAuthor = currentUserId != null && currentUserId == post.author.id
                    val comments = post.comments.map { comment ->
                        Comment(
                            id = comment.id,
                            authorName = "${comment.author.firstName.orEmpty()} ${comment.author.lastName.orEmpty()}".trim(),
                            text = comment.text,
                            createdAt = formatIsoDate(comment.createdAt),
                        )
                    }

                    when (userRole) {
                        UserRole.STUDENT -> {
                            val taskAnswer = post.taskAnswer
                            val submission = taskAnswer?.let { answer ->
                                Submission(
                                    submittedAt = answer.submittedAt?.let { formatIsoDate(it) } ?: "",
                                    files = answer.files.mapNotNull { it.fileName },
                                    score = answer.score,
                                    maxScore = answer.maxScore ?: post.maxScore,
                                    isNewGrade = false,
                                )
                            }
                            _uiState.value = TaskDetailUiState.StudentView(
                                task = task,
                                submission = submission,
                                taskAnswerId = taskAnswer?.id,
                                attachedFiles = emptyList(),
                                publicComments = comments,
                                privateComments = emptyList(),
                                commentInput = "",
                                selectedTab = StudentTab.PUBLIC_COMMENTS,
                                isAuthor = isAuthor,
                                courseId = courseId,
                            )
                            taskAnswer?.id?.let { loadPrivateComments(it) }
                        }
                        UserRole.TEACHER, UserRole.MAIN_TEACHER -> {
                            _uiState.value = TaskDetailUiState.TeacherView(
                                task = task,
                                publicComments = comments,
                                students = emptyList(),
                                commentInput = "",
                                selectedTab = TeacherTab.PUBLIC_COMMENTS,
                                isAuthor = isAuthor,
                                courseId = courseId,
                                canEdit = isAuthor || userRole == UserRole.TEACHER || userRole == UserRole.MAIN_TEACHER,
                            )
                        }
                    }
                }
                .onFailure {
                    sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка загрузки поста"))
                    sendEffect(TaskDetailUiEffect.NavigateBack)
                }
        }
    }

    private fun handleDeletePost() {
        viewModelScope.launch {
            postRepository.deletePost(courseId, postId)
                .onSuccess {
                    sendEffect(TaskDetailUiEffect.NavigateToCourseFeed(courseId))
                }
                .onFailure {
                    sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка удаления поста"))
                }
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
                when (state.selectedTab) {
                    StudentTab.PRIVATE_COMMENTS -> state.taskAnswerId?.let {
                        sendTaskAnswerComment(it, text)
                    } ?: run {
                        _uiState.value = state.copy(commentInput = text)
                        sendEffect(TaskDetailUiEffect.ShowError("Нет отправленной работы для личных комментариев"))
                    }
                    StudentTab.PUBLIC_COMMENTS -> sendPostComment(text)
                }
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

    private fun loadPrivateComments(taskAnswerId: String) {
        viewModelScope.launch {
            commentRepository.getTaskAnswerComments(taskAnswerId)
                .onSuccess { comments ->
                    val state = _uiState.value
                    if (state is TaskDetailUiState.StudentView && state.taskAnswerId == taskAnswerId) {
                        _uiState.value = state.copy(privateComments = comments)
                    }
                }
                .onFailure {
                    sendEffect(
                        TaskDetailUiEffect.ShowError(
                            it.message ?: "Ошибка загрузки личных комментариев"
                        )
                    )
                }
        }
    }

    private fun sendTaskAnswerComment(taskAnswerId: String, text: String) {
        viewModelScope.launch {
            commentRepository.createTaskAnswerComment(taskAnswerId, text)
                .onSuccess { loadPrivateComments(taskAnswerId) }
                .onFailure {
                    sendEffect(
                        TaskDetailUiEffect.ShowError(
                            it.message ?: "Ошибка отправки комментария"
                        )
                    )
                    val state = _uiState.value
                    if (state is TaskDetailUiState.StudentView) {
                        _uiState.value = state.copy(commentInput = text)
                    }
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

    companion object {
        private val isoFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        )

        fun formatIsoDate(isoString: String): String {
            val cleaned = isoString.trim()
            for (fmt in isoFormats) {
                try {
                    val date = fmt.parse(cleaned) ?: continue
                    val now = Calendar.getInstance()
                    val cal = Calendar.getInstance().apply { time = date }
                    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

                    return when {
                        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) ->
                            "Сегодня ${timeFmt.format(date)}"

                        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 ->
                            "Вчера ${timeFmt.format(date)}"

                        else -> {
                            val dateFmt = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                            dateFmt.format(date)
                        }
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            return isoString
        }
    }
}

private fun PostModel.toTaskDetail(): TaskDetail = TaskDetail(
    id = id,
    title = text.take(80),
    authorId = author.id,
    authorName = "${author.firstName.orEmpty()} ${author.lastName.orEmpty()}".trim(),
    createdAt = TaskDetailViewModel.formatIsoDate(createdAt),
    description = text,
    deadline = deadline?.let { TaskDetailViewModel.formatIsoDate(it) } ?: "Без дедлайна",
    maxScore = maxScore,
    files = files.map { TaskFile(id = it.id, fileName = it.fileName) },
    postType = postType.name,
)
