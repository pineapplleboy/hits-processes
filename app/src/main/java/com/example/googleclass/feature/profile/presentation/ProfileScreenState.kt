package com.example.googleclass.feature.profile.presentation

sealed interface ProfileScreenState {

    data object Loading : ProfileScreenState

    data class Content(
        val firstName: String,
        val lastName: String,
        val email: String,
        val city: String?,
        val birthday: String?,
    ) : ProfileScreenState

    data class Error(val message: String) : ProfileScreenState
}
