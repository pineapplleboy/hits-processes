package com.example.googleclass.common.network

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T, R> safeApiCall(
    apiCall: suspend () -> Response<T>,
    converter: (T) -> R
): Result<R> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(converter(body))
            } else {
                Result.failure(NullPointerException("Empty response"))
            }
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun safeApiCallUnit(
    apiCall: suspend () -> Response<Unit>
): Result<Unit> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun <T, R> safeApiCallNullable(
    apiCall: suspend () -> Response<T>,
    converter: (T) -> R,
): Result<R?> {
    return try {
        val response = apiCall()
        when {
            response.isSuccessful -> {
                val body = response.body()
                Result.success(if (body != null) converter(body) else null)
            }
            response.code() == 404 -> Result.success(null)
            else -> Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
