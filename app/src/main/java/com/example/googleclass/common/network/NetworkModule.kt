package com.example.googleclass.common.network

import com.example.googleclass.feature.authorization.data.SessionExpiredNotifierImpl
import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.data.TokenStorageImpl
import com.example.googleclass.feature.authorization.data.remote.AuthApi
import com.example.googleclass.feature.authorization.data.remote.AuthInterceptor
import com.example.googleclass.feature.authorization.data.remote.TokenAuthenticator
import com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import com.example.googleclass.feature.taskdetail.data.api.FileApi
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

    single<SessionExpiredNotifier> {
        SessionExpiredNotifierImpl()
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

    single<CoursesApi> {
        get<Retrofit>(named("authenticatedRetrofit")).create(CoursesApi::class.java)
    }

    single<FileApi> {
        get<Retrofit>(named("authenticatedRetrofit")).create(FileApi::class.java)
    }
}
