package com.example.googleclass.feature.authorization.data

import com.example.googleclass.feature.authorization.domain.model.TokenPair

interface TokenStorage {

    fun getTokens(): TokenPair?

    fun saveTokens(tokens: TokenPair)

    fun clearTokens()
}
