package com.example.googleclass.feature.taskdetail

import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.courses.data.remote.TaskAnswerApi
import com.example.googleclass.feature.taskdetail.data.repository.CommentRepositoryImpl
import com.example.googleclass.feature.taskdetail.data.repository.FileRepositoryImpl
import com.example.googleclass.feature.taskdetail.data.repository.TaskAnswerRepositoryImpl
import com.example.googleclass.feature.taskdetail.domain.repository.CommentRepository
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import com.example.googleclass.feature.taskdetail.domain.repository.TaskAnswerRepository
import com.example.googleclass.feature.taskdetail.presentation.TaskDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val taskDetailModule = module {
    single<FileRepository> { FileRepositoryImpl(get()) }
    single<CommentRepository> { CommentRepositoryImpl(get()) }
    single<TaskAnswerRepository> { TaskAnswerRepositoryImpl(get<TaskAnswerApi>()) }
    viewModel { (courseId: String, postId: String, userRole: UserRole) ->
        TaskDetailViewModel(
            courseId = courseId,
            postId = postId,
            userRole = userRole,
            postRepository = get(),
            commentRepository = get(),
            taskAnswerRepository = get(),
            fileRepository = get(),
            contentResolver = androidContext().contentResolver,
            userApi = get(),
        )
    }
}
