package com.example.googleclass.feature.course

import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.course.data.repository.CourseDetailRepositoryImpl
import com.example.googleclass.feature.course.domain.repository.CourseDetailRepository
import com.example.googleclass.feature.course.domain.usecase.GetCourseDetailUseCase
import com.example.googleclass.feature.course.data.remote.CourseDetailApi
import com.example.googleclass.feature.course.presentation.CourseScreenViewModel
import com.example.googleclass.feature.course.presentation.marks.MarksScreenViewModel
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val courseDetailModule = module {

    single<CourseDetailRepository> {
        CourseDetailRepositoryImpl(get(), get())
    }

    factory { GetCourseDetailUseCase(get()) }

    viewModel { (courseId: String) ->
        CourseScreenViewModel(
            courseId = courseId,
            getCourseDetailUseCase = get(),
            repository = get<CourseDetailRepository>(),
            coursesApi = get<CoursesApi>(),
            userApi = get<UserApi>(),
        )
    }

    viewModel { (courseId: String) ->
        MarksScreenViewModel(
            courseId = courseId,
            courseDetailApi = get<CourseDetailApi>(),
        )
    }
}

