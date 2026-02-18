package com.example.googleclass.feature.authorization.presentation

import com.example.googleclass.feature.authorization.domain.model.UserCredentials

sealed interface AuthorizationScreenState {

    data class Default(
        val credentials: UserCredentials,
    ) : AuthorizationScreenState

    data object Loading : AuthorizationScreenState
}