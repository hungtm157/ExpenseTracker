package com.example.expensetracker.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ApiClient — Singleton cung cấp instance Retrofit.
 * Thay BASE_URL bằng URL thực của API khi tích hợp backend.
 */
object ApiClient {

//    private const val BASE_URL = "https://baculine-kelsey-nonethically.ngrok-free.dev"
private const val BASE_URL = "https://maddie-conditioned-increasingly.ngrok-free.dev"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
