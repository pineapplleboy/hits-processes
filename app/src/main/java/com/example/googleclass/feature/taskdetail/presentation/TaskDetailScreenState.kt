package com.example.googleclass.feature.taskdetail.presentation

import android.net.Uri
import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail

sealed interface TaskDetailUiState {

    data object Loading : TaskDetailUiState

    data class StudentView(
        val task: TaskDetail,
        val submission: Submission?,
        val attachedFiles: List<AttachedFile>,
        val publicComments: List<Comment>,
        val privateComments: List<Comment>,
        val commentInput: String,
        val selectedTab: StudentTab,
    ) : TaskDetailUiState

    data class TeacherView(
        val task: TaskDetail,
        val publicComments: List<Comment>,
        val students: List<StudentSubmissionInfo>,
        val commentInput: String,
        val selectedTab: TeacherTab,
    ) : TaskDetailUiState
}

data class AttachedFile(
    val uri: Uri,
    val displayName: String,
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
    data object SendComment : TaskDetailUiEvent

    data class FileAttached(val uri: Uri, val displayName: String) : TaskDetailUiEvent
    data class FileRemoved(val uri: Uri) : TaskDetailUiEvent
    data class CommentInputChanged(val text: String) : TaskDetailUiEvent
    data class StudentTabSelected(val tab: StudentTab) : TaskDetailUiEvent
    data class TeacherTabSelected(val tab: TeacherTab) : TaskDetailUiEvent
    data class OpenStudentChat(
        val taskAnswerId: String,
        val studentName: String,
        val studentUserId: String,
    ) : TaskDetailUiEvent
    data class DownloadFile(val fileId: String) : TaskDetailUiEvent
}

sealed interface TaskDetailUiEffect {
    data object NavigateBack : TaskDetailUiEffect
    data class NavigateToStudentChat(
        val taskAnswerId: String,
        val studentName: String,
        val studentUserId: String,
    ) : TaskDetailUiEffect
    data class ShowError(val message: String) : TaskDetailUiEffect
    data class StartFileUpload(val uris: List<Uri>) : TaskDetailUiEffect
    data class StartFileDownload(val fileId: String) : TaskDetailUiEffect
    data object None: TaskDetailUiEffect
}
