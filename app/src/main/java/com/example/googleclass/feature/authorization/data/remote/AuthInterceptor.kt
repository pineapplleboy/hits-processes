package com.example.googleclass.feature.authorization.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (isAuthPath(request.url.encodedPath)) {
            return chain.proceed(request)
        }
        val token = tokenProvider()
        val newRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }

    private fun isAuthPath(path: String): Boolean {
        return path.contains("login") ||
            path.contains("register") ||
            path.contains("refresh-tokens")
    }
}
