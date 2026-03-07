package com.example.googleclass.feature.authorization.domain.repository

import com.example.googleclass.feature.authorization.domain.model.RegisterData
import com.example.googleclass.feature.authorization.domain.model.TokenPair
import com.example.googleclass.feature.authorization.domain.model.UserCredentials

interface AuthRepository {

    suspend fun login(credentials: UserCredentials): Result<TokenPair>

    suspend fun register(data: RegisterData): Result<TokenPair>

    suspend fun refreshTokens(): Result<TokenPair>

    suspend fun logout()
}
