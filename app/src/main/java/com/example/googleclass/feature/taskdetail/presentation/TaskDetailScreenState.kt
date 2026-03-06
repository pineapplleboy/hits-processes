package com.example.googleclass.feature.taskdetail.presentation

import com.example.googleclass.feature.taskdetail.domain.model.Comment
import com.example.googleclass.feature.taskdetail.domain.model.StudentSubmissionInfo
import com.example.googleclass.feature.taskdetail.domain.model.Submission
import com.example.googleclass.feature.taskdetail.domain.model.TaskDetail

sealed interface TaskDetailScreenState {

    data object Loading : TaskDetailScreenState

    data class StudentView(
        val task: TaskDetail,
        val submission: Submission?,
        val publicComments: List<Comment>,
        val privateComments: List<Comment>,
        val commentInput: String,
        val selectedTab: StudentTab,
    ) : TaskDetailScreenState

    data class TeacherView(
        val task: TaskDetail,
        val publicComments: List<Comment>,
        val students: List<StudentSubmissionInfo>,
        val commentInput: String,
        val selectedTab: TeacherTab,
    ) : TaskDetailScreenState
}

enum class StudentTab {
    PUBLIC_COMMENTS,
    PRIVATE_COMMENTS,
}

enum class TeacherTab {
    PUBLIC_COMMENTS,
    STUDENTS,
}
