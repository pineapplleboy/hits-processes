package com.example.googleclass.feature.authorization.data.remote

import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.data.remote.dto.RefreshTokenRequestDto
import com.example.googleclass.feature.authorization.data.remote.toDomain
import com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * При 401: запрашивает новый access-токен по refresh-токену и повторяет запрос.
 * Если refresh не удался (например, истёк) — очищает токены и уведомляет о выходе в авторизацию.
 */
class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
    private val sessionExpiredNotifier: SessionExpiredNotifier,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.contains("refresh-tokens")) {
            return null
        }
        val refreshToken = tokenStorage.getTokens()?.refreshToken ?: return null
        val newTokens = runBlocking {
            val resp = authApi.refreshTokens(RefreshTokenRequestDto(refreshToken))
            if (resp.isSuccessful) {
                resp.body()?.toDomain()
            } else {
                tokenStorage.clearTokens()
                sessionExpiredNotifier.notifySessionExpired()
                null
            }
        } ?: return null
        tokenStorage.saveTokens(newTokens)
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }
}
