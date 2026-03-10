package com.example.googleclass.feature.profile.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userApi: UserApi,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf<ProfileScreenState>(ProfileScreenState.Loading)
        private set

    /** true после успешного logout — сигнал для навигации */
    var logoutCompleted by mutableStateOf(false)
        private set

    init {
        loadProfile()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                Log.e(TAG, "logout: exception", e)
            } finally {
                logoutCompleted = true
            }
        }
    }

    private fun loadProfile() {
        state = ProfileScreenState.Loading
        viewModelScope.launch {
            try {
                val response = userApi.getMyProfile()
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        state = ProfileScreenState.Content(
                            firstName = user.firstName ?: "",
                            lastName = user.lastName ?: "",
                            email = user.email,
                            city = user.city,
                            birthday = user.birthday,
                        )
                    } else {
                        state = ProfileScreenState.Error("Пустой ответ сервера")
                    }
                } else {
                    state = ProfileScreenState.Error("Ошибка загрузки профиля: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadProfile: exception", e)
                state = ProfileScreenState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
