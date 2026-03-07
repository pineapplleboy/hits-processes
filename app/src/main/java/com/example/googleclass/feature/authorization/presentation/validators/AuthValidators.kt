package com.example.googleclass.feature.authorization.presentation.validators

private const val PASSWORD_MIN_LENGTH = 8
private const val PASSWORD_MAX_LENGTH = 32

fun isFullNameValid(fullName: String): Boolean {
    val parts = fullName.trim().split("\\s+".toRegex())
    if (parts.size != 2) return false
    val name = parts[0]
    val surname = parts[1]
    val regex = Regex("^[А-Яа-яЁё]{2,}$")
    return regex.matches(name) && regex.matches(surname)
}

fun isPasswordValid(password: String): Boolean {
    val length = password.length
    return length in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
}
