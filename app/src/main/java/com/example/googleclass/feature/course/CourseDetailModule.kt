package com.example.googleclass.feature.course

import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.course.data.repository.CourseDetailRepositoryImpl
import com.example.googleclass.feature.course.domain.repository.CourseDetailRepository
import com.example.googleclass.feature.course.domain.usecase.GetCourseDetailUseCase
import com.example.googleclass.feature.course.presentation.CourseDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val courseDetailModule = module {

    single<CourseDetailRepository> {
        CourseDetailRepositoryImpl(get(), get())
    }

    factory { GetCourseDetailUseCase(get()) }

    viewModel { (courseId: String) ->
        CourseDetailViewModel(
            courseId = courseId,
            getCourseDetailUseCase = get(),
            userApi = get<UserApi>(),
        )
    }
}

