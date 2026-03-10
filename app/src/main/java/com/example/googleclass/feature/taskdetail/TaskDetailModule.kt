package com.example.googleclass.feature.taskdetail

import com.example.googleclass.feature.taskdetail.data.repository.CommentRepositoryImpl
import com.example.googleclass.feature.taskdetail.data.repository.FileRepositoryImpl
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import com.example.googleclass.feature.taskdetail.presentation.TaskDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val taskDetailModule = module {
    single<FileRepository> { FileRepositoryImpl(get()) }
    single<CommentRepository> { CommentRepositoryImpl(get()) }
    viewModel { (postId: String) ->
        TaskDetailViewModel(postId = postId, commentRepository = get())
    }
}
