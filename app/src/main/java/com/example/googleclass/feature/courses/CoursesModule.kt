package com.example.googleclass.feature.courses

import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import com.example.googleclass.feature.courses.data.remote.TaskAnswerApi
import com.example.googleclass.feature.courses.presentation.ArchivedCoursesViewModel
import com.example.googleclass.feature.courses.presentation.CoursesScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val coursesModule = module {

    viewModel {
        CoursesScreenViewModel(
            coursesApi = get<CoursesApi>(),
            userApi = get<UserApi>(),
            authRepository = get<AuthRepository>(),
            taskAnswerApi = get<TaskAnswerApi>(),
        )
    }

    viewModel {
        ArchivedCoursesViewModel(
            coursesApi = get<CoursesApi>(),
        )
    }
}
