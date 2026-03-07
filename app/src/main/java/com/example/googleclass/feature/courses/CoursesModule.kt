package com.example.googleclass.feature.courses

import com.example.googleclass.feature.courses.data.remote.CoursesApi
import com.example.googleclass.feature.courses.presentation.CoursesScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val coursesModule = module {

    viewModel { CoursesScreenViewModel(get<CoursesApi>()) }
}
