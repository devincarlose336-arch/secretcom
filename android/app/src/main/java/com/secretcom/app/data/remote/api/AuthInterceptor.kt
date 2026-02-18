package com.secretcom.app.data.remote.api

import com.secretcom.app.data.preferences.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { tokenManager.accessToken.firstOrNull() }

        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)

        if (response.code == 401) {
            val refreshToken = runBlocking { tokenManager.refreshToken.firstOrNull() }
            if (!refreshToken.isNullOrEmpty()) {
                runBlocking { tokenManager.clearTokens() }
            }
        }

        return response
    }
}
