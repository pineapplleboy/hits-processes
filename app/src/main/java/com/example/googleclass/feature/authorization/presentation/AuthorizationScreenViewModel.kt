package com.example.googleclass.feature.authorization.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.authorization.data.repository.AuthException
import com.example.googleclass.feature.authorization.domain.model.RegisterData
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import com.example.googleclass.feature.authorization.domain.usecase.LoginUseCase
import com.example.googleclass.feature.authorization.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorizationScreenViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<AuthorizationScreenState> = MutableStateFlow(
        AuthorizationScreenState.Default(credentials = UserCredentials(login = "", password = ""))
    )
    val state: StateFlow<AuthorizationScreenState> = _state.asStateFlow()

    fun onLoginClick() {
        val current = _state.value
        if (current !is AuthorizationScreenState.Default) return
        val credentials = current.credentials
        if (credentials.login.isBlank() || credentials.password.isBlank()) return

        viewModelScope.launch {
            _state.value = AuthorizationScreenState.Loading
            loginUseCase(credentials)
                .onSuccess {
                    _state.value = AuthorizationScreenState.AuthSuccess
                }
                .onFailure { e ->
                    val message = "Ошибка входа"
                    _state.value = AuthorizationScreenState.Default(
                        credentials = credentials,
                        errorMessage = message,
                    )
                }
        }
    }

    fun onRegisterClick(data: RegisterData) {
        viewModelScope.launch {
            _state.value = AuthorizationScreenState.Loading
            registerUseCase(data)
                .onSuccess {
                    _state.value = AuthorizationScreenState.AuthSuccess
                }
                .onFailure { e ->
                    val message = "Ошибка регистрации"
                    _state.value = AuthorizationScreenState.Default(
                        credentials = _state.value.let { if (it is AuthorizationScreenState.Default) it.credentials else UserCredentials("", "") },
                        errorMessage = message,
                    )
                }
        }
    }

    fun onLoginChange(login: String) {
        val currentState = _state.value
        if (currentState is AuthorizationScreenState.Default) {
            _state.value = currentState.copy(
                credentials = currentState.credentials.copy(login = login),
                errorMessage = null,
            )
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _state.value
        if (currentState is AuthorizationScreenState.Default) {
            _state.value = currentState.copy(
                credentials = currentState.credentials.copy(password = password),
                errorMessage = null,
            )
        }
    }

    fun clearError() {
        val current = _state.value
        if (current is AuthorizationScreenState.Default) {
            _state.value = current.copy(errorMessage = null)
        }
    }
}
