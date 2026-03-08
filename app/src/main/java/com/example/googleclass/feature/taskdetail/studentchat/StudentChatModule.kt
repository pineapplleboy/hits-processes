package com.example.googleclass.feature.taskdetail.studentchat

import com.example.googleclass.feature.taskdetail.studentchat.presentation.StudentChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val studentChatModule = module {
    viewModel { (studentId: String, studentName: String) ->
        StudentChatViewModel(studentId = studentId, studentName = studentName)
    }
}
