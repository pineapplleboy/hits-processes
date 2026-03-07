package com.example.googleclass.feature.authorization.domain.model

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)
