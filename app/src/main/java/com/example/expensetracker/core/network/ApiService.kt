package com.example.expensetracker.core.network

import com.example.expensetracker.data.models.LoginRequest
import com.example.expensetracker.data.models.LoginResponse
import com.example.expensetracker.data.models.RegisterRequest
import com.example.expensetracker.data.models.RegisterResponse
import com.example.expensetracker.data.models.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * ApiService — Interface định nghĩa các API endpoint.
 */
interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register/send-otp")
    suspend fun sendOtp(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/auth/register/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<RegisterResponse>

    @GET("ping")
    suspend fun ping(): Any
}
