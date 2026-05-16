package com.example.googleclass.feature.taskdetail.studentchat

import com.example.googleclass.feature.taskdetail.studentchat.presentation.StudentChatScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val studentChatModule = module {
    viewModel { (taskAnswerId: String, studentName: String, studentUserId: String, currentUserId: String) ->
        StudentChatScreenViewModel(
            taskAnswerId = taskAnswerId,
            studentName = studentName,
            studentUserId = studentUserId,
            currentUserId = currentUserId,
            commentRepository = get(),
        )
    }
}
