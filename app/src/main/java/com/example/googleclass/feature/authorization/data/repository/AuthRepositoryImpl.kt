package com.example.googleclass.feature.authorization.data.repository

import com.example.googleclass.common.network.safeApiCall
import com.example.googleclass.common.network.safeApiCallUnit
import com.example.googleclass.feature.authorization.data.remote.AuthApi
import com.example.googleclass.feature.authorization.data.remote.dto.RefreshTokenRequestDto
import com.example.googleclass.feature.authorization.data.remote.toDomain
import com.example.googleclass.feature.authorization.data.remote.toLoginDto
import com.example.googleclass.feature.authorization.data.remote.toRegisterDto
import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.domain.model.RegisterData
import com.example.googleclass.feature.authorization.domain.model.TokenPair
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(credentials: UserCredentials): Result<TokenPair> = withContext(Dispatchers.IO) {
        safeApiCall(
            apiCall = { api.login(credentials.toLoginDto()) },
            converter = { it.toDomain() }
        ).mapCatching { tokens ->
            tokenStorage.saveTokens(tokens)
            tokens
        }.recoverCatching { e ->
            when (e) {
                is HttpException -> throw AuthException(e.code(), e.response()?.errorBody()?.string() ?: e.message())
                else -> throw AuthException(-1, e.message ?: "Ошибка входа")
            }
        }
    }

    override suspend fun register(data: RegisterData): Result<TokenPair> = withContext(Dispatchers.IO) {
        safeApiCall(
            apiCall = { api.register(data.toRegisterDto()) },
            converter = { it.toDomain() }
        ).mapCatching { tokens ->
            tokenStorage.saveTokens(tokens)
            tokens
        }.recoverCatching { e ->
            when (e) {
                is HttpException -> throw AuthException(e.code(), e.response()?.errorBody()?.string() ?: e.message())
                else -> throw AuthException(-1, e.message ?: "Ошибка регистрации")
            }
        }
    }

    override suspend fun refreshTokens(): Result<TokenPair> = withContext(Dispatchers.IO) {
        val current = tokenStorage.getTokens()
            ?: return@withContext Result.failure(AuthException(-1, "No refresh token"))
        safeApiCall(
            apiCall = { api.refreshTokens(RefreshTokenRequestDto(current.refreshToken)) },
            converter = { it.toDomain() }
        ).mapCatching { tokens ->
            tokenStorage.saveTokens(tokens)
            tokens
        }.recoverCatching { e ->
            when (e) {
                is HttpException -> throw AuthException(e.code(), e.response()?.errorBody()?.string() ?: e.message())
                else -> throw AuthException(-1, e.message ?: "Ошибка обновления токена")
            }
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        safeApiCallUnit(apiCall = { api.logout() })
        tokenStorage.clearTokens()
    }
}

class AuthException(val code: Int, override val message: String) : Exception(message)
