package com.example.googleclass.feature.authorization.domain.usecase

import com.example.googleclass.feature.authorization.domain.model.RegisterData
import com.example.googleclass.feature.authorization.domain.model.TokenPair
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository,
) {

    suspend operator fun invoke(data: RegisterData): Result<TokenPair> =
        repository.register(data)
}
