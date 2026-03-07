package com.example.googleclass.feature.authorization.data.network

import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.data.TokenStorageImpl
import com.example.googleclass.feature.authorization.data.remote.AuthApi
import com.example.googleclass.feature.authorization.data.remote.AuthInterceptor
import com.example.googleclass.feature.authorization.data.remote.TokenAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://91.227.18.176/hits-class/"

val networkModule = module {

    single<TokenStorage> { TokenStorageImpl(get()) }

    single {
        val tokenStorage: TokenStorage = get()
        AuthInterceptor(tokenProvider = { tokenStorage.getTokens()?.accessToken })
    }

    single(named("authClient")) {
        val authInterceptor: AuthInterceptor = get()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get(named("authClient")))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }

    single<com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier> {
        com.example.googleclass.feature.authorization.data.SessionExpiredNotifierImpl()
    }

    single {
        TokenAuthenticator(get(), get(), get())
    }

    single(named("authenticatedClient")) {
        val authInterceptor: AuthInterceptor = get()
        val tokenAuthenticator: TokenAuthenticator = get()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(named("authenticatedRetrofit")) {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get(named("authenticatedClient")))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<com.example.googleclass.feature.courses.data.remote.CoursesApi> {
        get<Retrofit>(named("authenticatedRetrofit")).create(com.example.googleclass.feature.courses.data.remote.CoursesApi::class.java)
    }
}
