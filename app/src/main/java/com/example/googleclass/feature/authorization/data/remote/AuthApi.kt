package com.example.googleclass.feature.authorization.data.remote

import com.example.googleclass.feature.authorization.data.remote.dto.RefreshTokenRequestDto
import com.example.googleclass.feature.authorization.data.remote.dto.TokenResponseDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserLoginDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserRegisterDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/user/login")
    suspend fun login(@Body body: UserLoginDto): Response<TokenResponseDto>

    @POST("api/v1/user/register")
    suspend fun register(@Body body: UserRegisterDto): Response<TokenResponseDto>

    @POST("api/v1/user/refresh-tokens")
    suspend fun refreshTokens(@Body body: RefreshTokenRequestDto): Response<TokenResponseDto>

    @POST("api/v1/user/logout")
    suspend fun logout(): Response<Unit>
}
