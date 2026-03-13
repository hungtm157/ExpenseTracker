package com.example.expensetracker.core.network

import com.example.expensetracker.App
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient — Singleton cung cấp instance Retrofit.
 * Thay BASE_URL bằng URL thực của API khi tích hợp backend.
 */
object ApiClient {

    private const val BASE_URL = "https://maddie-conditioned-increasingly.ngrok-free.dev"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = App.instance.preferences.authToken

        val newRequest = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
