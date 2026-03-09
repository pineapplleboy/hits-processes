package com.example.googleclass.common.network

import com.example.googleclass.common.network.dto.UserModel
import retrofit2.Response
import retrofit2.http.GET

interface UserApi {

    @GET("api/v1/user")
    suspend fun getMyProfile(): Response<UserModel>
}

