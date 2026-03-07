package com.example.googleclass.feature.authorization.domain

import kotlinx.coroutines.flow.Flow

/**
 * Уведомляет о истечении сессии (refresh-токен недействителен).
 * Приложение должно перенаправить пользователя на экран авторизации.
 */
interface SessionExpiredNotifier {

    val sessionExpiredEvents: Flow<Unit>

    fun notifySessionExpired()
}
