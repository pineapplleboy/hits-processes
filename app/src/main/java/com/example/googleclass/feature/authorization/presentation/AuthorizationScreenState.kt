package com.example.googleclass.feature.authorization.presentation

import com.example.googleclass.feature.authorization.domain.model.UserCredentials

sealed interface AuthorizationScreenState {

    data class Default(
        val credentials: UserCredentials,
        val errorMessage: String? = null,
    ) : AuthorizationScreenState

    data object Loading : AuthorizationScreenState

    data class Error(val message: String) : AuthorizationScreenState

    data object AuthSuccess : AuthorizationScreenState
}