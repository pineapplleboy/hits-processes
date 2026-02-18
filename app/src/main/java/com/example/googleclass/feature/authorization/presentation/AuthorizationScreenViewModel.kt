package com.example.googleclass.feature.authorization.presentation

import androidx.lifecycle.ViewModel
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthorizationScreenViewModel : ViewModel() {

    private val _state: MutableStateFlow<AuthorizationScreenState> = MutableStateFlow(
        value = AuthorizationScreenState.Default(
            credentials = UserCredentials("", "")
        )
    )
    val state: StateFlow<AuthorizationScreenState> = _state.asStateFlow()

    fun onLoginClick() {

    }

    fun onLoginChange(login: String) {
        val currentState = _state.value
        if (currentState is AuthorizationScreenState.Default) {
            _state.value = currentState.copy(
                credentials = currentState.credentials.copy(
                    login = login,
                )
            )
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _state.value
        if (currentState is AuthorizationScreenState.Default) {
            _state.value = currentState.copy(
                credentials = currentState.credentials.copy(
                    password = password,
                )
            )
        }
    }
}