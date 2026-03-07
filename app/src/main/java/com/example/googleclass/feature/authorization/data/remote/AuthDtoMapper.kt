package com.example.googleclass.feature.authorization.data.remote

import com.example.googleclass.feature.authorization.data.remote.dto.TokenResponseDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserLoginDto
import com.example.googleclass.feature.authorization.data.remote.dto.UserRegisterDto
import com.example.googleclass.feature.authorization.domain.model.RegisterData
import com.example.googleclass.feature.authorization.domain.model.TokenPair
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import java.text.SimpleDateFormat
import java.util.Locale

/** Конвертирует дату в формат ISO (yyyy-MM-dd) для бэкенда (Java LocalDate). */
private fun toApiDateString(birthday: String): String {
    val s = birthday.trim()
    if (s.isBlank()) return birthday
    return try {
        val inputDdMm = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT).parse(s)
        val inputIso = inputDdMm ?: SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(s)
        if (inputIso != null) SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(inputIso) else s
    } catch (_: Exception) {
        s
    }
}

fun TokenResponseDto.toDomain(): TokenPair = TokenPair(
    accessToken = accessToken,
    refreshToken = refreshToken,
)

fun UserCredentials.toLoginDto(): UserLoginDto = UserLoginDto(
    email = login,
    password = password,
)

fun RegisterData.toRegisterDto(): UserRegisterDto = UserRegisterDto(
    email = email,
    password = password,
    firstName = firstName,
    lastName = lastName,
    birthday = toApiDateString(birthday),
    city = city,
)
