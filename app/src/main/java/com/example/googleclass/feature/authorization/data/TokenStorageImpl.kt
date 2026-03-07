package com.example.googleclass.feature.authorization.data

import android.content.Context
import android.content.SharedPreferences
import com.example.googleclass.feature.authorization.domain.model.TokenPair

class TokenStorageImpl(
    context: Context,
) : TokenStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    override fun getTokens(): TokenPair? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        return if (accessToken != null && refreshToken != null) {
            TokenPair(accessToken = accessToken, refreshToken = refreshToken)
        } else {
            null
        }
    }

    override fun saveTokens(tokens: TokenPair) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .apply()
    }

    override fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
