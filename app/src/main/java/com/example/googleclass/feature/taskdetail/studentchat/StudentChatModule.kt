package com.example.googleclass.feature.taskdetail.studentchat

import com.example.googleclass.feature.taskdetail.studentchat.presentation.StudentChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val studentChatModule = module {
    viewModel { (taskAnswerId: String, studentName: String, studentUserId: String) ->
        StudentChatViewModel(
            taskAnswerId = taskAnswerId,
            studentName = studentName,
            studentUserId = studentUserId,
            commentRepository = get(),
        )
    }
}
