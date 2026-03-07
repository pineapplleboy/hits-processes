package com.example.googleclass.feature.authorization.data.remote

import com.example.googleclass.feature.authorization.data.remote.dto.RefreshTokenRequestDto
import com.example.googleclass.feature.authorization.data.remote.dto.TokenResponseDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserLoginDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserRegisterDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: UserLoginDto): Response<TokenResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: UserRegisterDto): Response<TokenResponseDto>

    @POST("api/v1/auth/refresh-tokens")
    suspend fun refreshTokens(@Body body: RefreshTokenRequestDto): Response<TokenResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>
}
