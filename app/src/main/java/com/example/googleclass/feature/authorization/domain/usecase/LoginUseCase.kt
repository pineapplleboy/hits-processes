package com.example.googleclass.feature.authorization.domain.usecase

import com.example.googleclass.feature.authorization.domain.model.TokenPair
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
) {

    suspend operator fun invoke(credentials: UserCredentials): Result<TokenPair> =
        repository.login(credentials)
}
