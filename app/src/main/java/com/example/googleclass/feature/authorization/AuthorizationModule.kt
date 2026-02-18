package com.example.googleclass.feature.authorization

import com.example.googleclass.feature.authorization.presentation.AuthorizationScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authorizationModule = module {

//    single<AuthRepository> { AuthRepositoryImpl() }

//    factory { LoginUseCase(get()) }

    viewModel { AuthorizationScreenViewModel() }
}