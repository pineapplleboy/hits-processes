package com.example.googleclass.feature.authorization

import com.example.googleclass.feature.authorization.data.repository.AuthRepositoryImpl
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import com.example.googleclass.feature.authorization.domain.usecase.LoginUseCase
import com.example.googleclass.feature.authorization.domain.usecase.RegisterUseCase
import com.example.googleclass.feature.authorization.presentation.AuthorizationScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authorizationModule = module {

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory { LoginUseCase(get()) }

    factory { RegisterUseCase(get()) }

    viewModel { AuthorizationScreenViewModel(get(), get()) }
}