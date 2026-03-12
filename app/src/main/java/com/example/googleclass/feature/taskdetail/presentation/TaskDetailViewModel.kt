package com.example.googleclass.feature.taskdetail.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.post.data.model.PostModel
import com.example.googleclass.feature.post.data.model.PostType
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.SubmissionStatus
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail
import com.example.googleclass.feature.taskdetail.domain.model.TaskFile
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import com.example.googleclass.feature.taskdetail.domain.repository.TaskAnswerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val SUBMITTED_STATUSES = setOf("SUBMITTED", "COMPLETED", "COMPETED_AFTER_DEADLINE")

private data class TaskAnswerState(
    val id: String,
    val status: String,
    val files: List<TaskAnswerFileInfo>,
    val score: Int?,
    val submittedAt: String?,
    val maxScore: Int,
)

class TaskDetailViewModel(
    private val courseId: String,
    private val postId: String,
    private val userRole: UserRole,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val taskAnswerRepository: TaskAnswerRepository,
    private val fileRepository: FileRepository,
    private val contentResolver: ContentResolver,
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
            is TaskDetailUiEvent.UnsubmitWork -> handleUnsubmitWork()
            is TaskDetailUiEvent.SendComment -> handleSendComment()
            is TaskDetailUiEvent.FileAttached -> handleFileAttached(event.uri, event.displayName)
            is TaskDetailUiEvent.FileRemoved -> handleFileRemoved(event.fileId)
            is TaskDetailUiEvent.CommentInputChanged -> handleCommentInput(event.text)
            is TaskDetailUiEvent.StudentTabSelected -> handleStudentTab(event.tab)
            is TaskDetailUiEvent.TeacherTabSelected -> handleTeacherTab(event.tab)
            is TaskDetailUiEvent.OpenStudentChat -> {
                val state = _uiState.value
                val currentUserId = (state as? TaskDetailUiState.TeacherView)?.currentUserId ?: ""
                sendEffect(
                    TaskDetailUiEffect.NavigateToStudentChat(
                        taskAnswerId = event.taskAnswerId,
                        studentName = event.studentName,
                        studentUserId = event.studentUserId,
                        currentUserId = currentUserId,
                    )
                )
            }
            is TaskDetailUiEvent.DownloadFile -> sendEffect(
                TaskDetailUiEffect.StartFileDownload(event.fileId)
            )
            is TaskDetailUiEvent.EditPost -> sendEffect(
                TaskDetailUiEffect.NavigateToEdit(courseId = courseId, postId = postId)
            )
            is TaskDetailUiEvent.DeletePost -> handleDeletePost()
            is TaskDetailUiEvent.EvaluateStudent -> handleEvaluateStudent(event)
            is TaskDetailUiEvent.SetEvaluateScore -> handleSetEvaluateScore(event.score)
            is TaskDetailUiEvent.SubmitEvaluate -> handleSubmitEvaluate()
            is TaskDetailUiEvent.DismissEvaluateDialog -> handleDismissEvaluateDialog()
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            try {
                loadPostInternal()
            } catch (e: Exception) {
                sendEffect(TaskDetailUiEffect.ShowError(e.message ?: "Ошибка сети. Проверьте подключение."))
                sendEffect(TaskDetailUiEffect.NavigateBack)
            }
        }
    }

    private suspend fun loadPostInternal() {
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
                    val isTaskPost = post.postType == PostType.TASK

                    when (userRole) {
                        UserRole.STUDENT -> {
                            val taskAnswerFromPost = post.taskAnswer
                            val taskAnswerFromApi = if (isTaskPost && taskAnswerFromPost == null) {
                                taskAnswerRepository.getUserPostTaskAnswer(postId).getOrNull()
                            } else null
                            val (tid, status, files, score, submittedAt, maxScore) = when {
                                taskAnswerFromPost != null -> TaskAnswerState(
                                    id = taskAnswerFromPost.id,
                                    status = taskAnswerFromPost.status,
                                    files = taskAnswerFromPost.files.map { TaskAnswerFileInfo(it.id, it.fileName ?: "Файл") },
                                    score = taskAnswerFromPost.score,
                                    submittedAt = taskAnswerFromPost.submittedAt,
                                    maxScore = taskAnswerFromPost.maxScore ?: post.maxScore,
                                )
                                taskAnswerFromApi != null -> TaskAnswerState(
                                    id = taskAnswerFromApi.id,
                                    status = taskAnswerFromApi.status,
                                    files = taskAnswerFromApi.files.map { TaskAnswerFileInfo(it.id, it.fileName ?: "Файл") },
                                    score = taskAnswerFromApi.score,
                                    submittedAt = taskAnswerFromApi.submittedAt,
                                    maxScore = taskAnswerFromApi.maxScore ?: post.maxScore,
                                )
                                else -> null
                            } ?: run {
                                _uiState.value = TaskDetailUiState.StudentView(
                                    task = post.toTaskDetail(),
                                    submission = null,
                                    taskAnswerId = null,
                                    taskAnswerStatus = "",
                                    taskAnswerFiles = emptyList(),
                                    publicComments = post.comments.map { c ->
                                        Comment(c.id, "${c.author.firstName.orEmpty()} ${c.author.lastName.orEmpty()}".trim(), c.text, formatIsoDate(c.createdAt))
                                    },
                                    privateComments = emptyList(),
                                    commentInput = "",
                                    selectedTab = StudentTab.PUBLIC_COMMENTS,
                                    isAuthor = isAuthor,
                                    courseId = courseId,
                                )
                                return@onSuccess
                            }
                            val submission = if (SUBMITTED_STATUSES.contains(status.uppercase())) {
                                Submission(
                                    submittedAt = submittedAt?.let { formatIsoDate(it) } ?: "",
                                    files = files.map { it.fileName },
                                    score = score,
                                    maxScore = maxScore,
                                    isNewGrade = false,
                                )
                            } else null
                            _uiState.value = TaskDetailUiState.StudentView(
                                task = task,
                                submission = submission,
                                taskAnswerId = tid,
                                taskAnswerStatus = status,
                                taskAnswerFiles = files,
                                publicComments = comments,
                                privateComments = emptyList(),
                                commentInput = "",
                                selectedTab = StudentTab.PUBLIC_COMMENTS,
                                isAuthor = isAuthor,
                                courseId = courseId,
                            )
                            loadPrivateComments(tid)
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
                                currentUserId = currentUserId ?: "",
                            )
                            if (isTaskPost) loadTaskStudents(post.maxScore)
                        }
                    }
                }
                .onFailure {
                    sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка загрузки поста"))
                    sendEffect(TaskDetailUiEffect.NavigateBack)
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
        if (state is TaskDetailUiState.StudentView &&
            state.submission == null &&
            state.taskAnswerId != null &&
            !state.isUploadingFile
        ) {
            _uiState.value = state.copy(isUploadingFile = true)
            viewModelScope.launch {
                val uploadResult = fileRepository.uploadFile(uri, contentResolver) { }
                val currentState = _uiState.value as? TaskDetailUiState.StudentView ?: return@launch
                _uiState.value = currentState.copy(isUploadingFile = false)
                uploadResult
                    .onSuccess { fileModel ->
                        taskAnswerRepository.appendFiles(
                            currentState.taskAnswerId!!,
                            listOf(com.example.googleclass.feature.taskdetail.domain.model.TaskAnswerFile(fileModel.id, fileModel.fileName)),
                        )
                            .onSuccess { refreshStudentTaskAnswer(currentState.taskAnswerId!!) }
                            .onFailure {
                                sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка прикрепления файла"))
                            }
                    }
                    .onFailure {
                        sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка загрузки файла"))
                    }
            }
        }
    }

    private fun handleFileRemoved(fileId: String) {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView && state.taskAnswerId != null && state.submission == null) {
            viewModelScope.launch {
                taskAnswerRepository.unpinFile(state.taskAnswerId!!, fileId)
                    .onSuccess { refreshStudentTaskAnswer(state.taskAnswerId!!) }
                    .onFailure {
                        sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка удаления файла"))
                    }
            }
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
        if (state is TaskDetailUiState.StudentView && state.taskAnswerId != null && state.submission == null) {
            if (state.taskAnswerFiles.isEmpty()) {
                sendEffect(TaskDetailUiEffect.ShowError("Прикрепите хотя бы один файл"))
                return
            }
            viewModelScope.launch {
                taskAnswerRepository.submitTask(state.taskAnswerId!!)
                    .onSuccess { refreshStudentTaskAnswer(state.taskAnswerId!!) }
                    .onFailure {
                        sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка отправки работы"))
                    }
            }
        }
    }

    private fun handleUnsubmitWork() {
        val state = _uiState.value
        if (state is TaskDetailUiState.StudentView && state.taskAnswerId != null && state.submission != null) {
            viewModelScope.launch {
                taskAnswerRepository.unsubmitTask(state.taskAnswerId!!)
                    .onSuccess { refreshStudentTaskAnswer(state.taskAnswerId!!) }
                    .onFailure {
                        sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка отмены отправки"))
                    }
            }
        }
    }

    private fun refreshStudentTaskAnswer(taskAnswerId: String) {
        viewModelScope.launch {
            taskAnswerRepository.getUserPostTaskAnswer(postId)
                .getOrNull()
                ?.let { ta ->
                    val state = _uiState.value as? TaskDetailUiState.StudentView ?: return@launch
                    if (state.taskAnswerId != taskAnswerId) return@launch
                    val submission = if (SUBMITTED_STATUSES.contains(ta.status.uppercase())) {
                        Submission(
                            submittedAt = ta.submittedAt?.let { formatIsoDate(it) } ?: "",
                            files = ta.files.map { it.fileName ?: "Файл" },
                            score = ta.score,
                            maxScore = ta.maxScore ?: state.task.maxScore,
                            isNewGrade = false,
                        )
                    } else null
                    _uiState.value = state.copy(
                        submission = submission,
                        taskAnswerStatus = ta.status,
                        taskAnswerFiles = ta.files.map { TaskAnswerFileInfo(it.id, it.fileName ?: "Файл") },
                    )
                }
        }
    }

    private fun loadTaskStudents(maxScore: Int) {
        viewModelScope.launch {
            taskAnswerRepository.getAllPostTaskAnswers(postId)
                .onSuccess { taskAnswers ->
                    val state = _uiState.value as? TaskDetailUiState.TeacherView ?: return@onSuccess
                    val students = taskAnswers
                        .filter { ta -> SUBMITTED_STATUSES.contains(ta.status.uppercase()) }
                        .map { ta ->
                            StudentSubmissionInfo(
                                studentId = ta.userId ?: "",
                                studentName = ta.userName ?: "Студент",
                                taskAnswerId = ta.id,
                                score = ta.score,
                                maxScore = ta.maxScore ?: maxScore,
                                status = SubmissionStatus.SUBMITTED,
                            )
                        }
                    _uiState.value = state.copy(students = students)
                }
                .onFailure {
                    sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка загрузки работ студентов"))
                }
        }
    }

    private fun handleEvaluateStudent(event: TaskDetailUiEvent.EvaluateStudent) {
        val state = _uiState.value
        if (state is TaskDetailUiState.TeacherView) {
            _uiState.value = state.copy(
                evaluateDialog = EvaluateDialogState(
                    taskAnswerId = event.taskAnswerId,
                    studentName = event.studentName,
                    maxScore = event.maxScore,
                    score = 0,
                ),
            )
        }
    }

    private fun handleSetEvaluateScore(score: Int) {
        val state = _uiState.value
        if (state is TaskDetailUiState.TeacherView && state.evaluateDialog != null) {
            _uiState.value = state.copy(
                evaluateDialog = state.evaluateDialog.copy(score = score.coerceIn(0, state.evaluateDialog.maxScore)),
            )
        }
    }

    private fun handleSubmitEvaluate() {
        val state = _uiState.value
        if (state is TaskDetailUiState.TeacherView) {
            val dialog = state.evaluateDialog ?: return
            viewModelScope.launch {
                taskAnswerRepository.evaluateTask(dialog.taskAnswerId, dialog.score)
                    .onSuccess {
                        _uiState.value = state.copy(evaluateDialog = null)
                        loadTaskStudents(state.task.maxScore)
                    }
                    .onFailure {
                        sendEffect(TaskDetailUiEffect.ShowError(it.message ?: "Ошибка выставления оценки"))
                    }
            }
        }
    }

    private fun handleDismissEvaluateDialog() {
        val state = _uiState.value
        if (state is TaskDetailUiState.TeacherView) {
            _uiState.value = state.copy(evaluateDialog = null)
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
