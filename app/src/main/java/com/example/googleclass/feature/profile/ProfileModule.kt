package com.example.googleclass.feature.profile

import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import com.example.googleclass.feature.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

    viewModel {
        ProfileViewModel(
            userApi = get<UserApi>(),
            authRepository = get<AuthRepository>(),
        )
    }
}
