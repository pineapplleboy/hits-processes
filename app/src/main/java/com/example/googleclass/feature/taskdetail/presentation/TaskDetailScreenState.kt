package com.example.googleclass.feature.taskdetail.presentation

import android.net.Uri
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail

sealed interface TaskDetailUiState {

    data object Loading : TaskDetailUiState

    data class StudentView(
        val task: TaskDetail,
        val submission: Submission?,
        val taskAnswerId: String? = null,
        val taskAnswerStatus: String = "",
        val taskAnswerFiles: List<TaskAnswerFileInfo> = emptyList(),
        val isUploadingFile: Boolean = false,
        val publicComments: List<Comment>,
        val privateComments: List<Comment>,
        val commentInput: String,
        val selectedTab: StudentTab,
        val isAuthor: Boolean = false,
        val courseId: String = "",
    ) : TaskDetailUiState

    data class TeacherView(
        val task: TaskDetail,
        val publicComments: List<Comment>,
        val students: List<StudentSubmissionInfo>,
        val commentInput: String,
        val selectedTab: TeacherTab,
        val isAuthor: Boolean = false,
        val courseId: String = "",
        val canEdit: Boolean = false,
        val evaluateDialog: EvaluateDialogState? = null,
        val currentUserId: String = "",
    ) : TaskDetailUiState
}

data class EvaluateDialogState(
    val taskAnswerId: String,
    val studentName: String,
    val maxScore: Int,
    val score: Int = 0,
)

data class AttachedFile(
    val uri: Uri,
    val displayName: String,
)

data class TaskAnswerFileInfo(
    val id: String,
    val fileName: String,
)

enum class StudentTab {
    PUBLIC_COMMENTS,
    PRIVATE_COMMENTS,
}

enum class TeacherTab {
    PUBLIC_COMMENTS,
    STUDENTS,
}

sealed interface TaskDetailUiEvent {
    data object NavigateBack : TaskDetailUiEvent
    data object SubmitWork : TaskDetailUiEvent
    data object UnsubmitWork : TaskDetailUiEvent
    data object SendComment : TaskDetailUiEvent
    data object EditPost : TaskDetailUiEvent
    data object DeletePost : TaskDetailUiEvent

    data class FileAttached(val uri: Uri, val displayName: String) : TaskDetailUiEvent
    data class FileRemoved(val fileId: String) : TaskDetailUiEvent
    data class CommentInputChanged(val text: String) : TaskDetailUiEvent
    data class StudentTabSelected(val tab: StudentTab) : TaskDetailUiEvent
    data class TeacherTabSelected(val tab: TeacherTab) : TaskDetailUiEvent
    data class OpenStudentChat(
        val taskAnswerId: String,
        val studentName: String,
        val studentUserId: String,
    ) : TaskDetailUiEvent
    data class DownloadFile(val fileId: String) : TaskDetailUiEvent
    data class EvaluateStudent(val taskAnswerId: String, val studentName: String, val maxScore: Int) : TaskDetailUiEvent
    data class SetEvaluateScore(val score: Int) : TaskDetailUiEvent
    data object SubmitEvaluate : TaskDetailUiEvent
    data object DismissEvaluateDialog : TaskDetailUiEvent
}

sealed interface TaskDetailUiEffect {
    data object NavigateBack : TaskDetailUiEffect
    data class NavigateToEdit(
        val courseId: String,
        val postId: String,
    ) : TaskDetailUiEffect
    data class NavigateToCourseFeed(val courseId: String) : TaskDetailUiEffect
    data class NavigateToStudentChat(
        val taskAnswerId: String,
        val studentName: String,
        val studentUserId: String,
        val currentUserId: String,
    ) : TaskDetailUiEffect
    data class ShowError(val message: String) : TaskDetailUiEffect
    data class StartFileUpload(val uris: List<Uri>) : TaskDetailUiEffect
    data class StartFileDownload(val fileId: String) : TaskDetailUiEffect
}
