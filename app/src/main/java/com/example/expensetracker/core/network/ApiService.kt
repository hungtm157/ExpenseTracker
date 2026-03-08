package com.example.expensetracker.core.network

import com.example.expensetracker.data.models.ForgotPasswordRequest
import com.example.expensetracker.data.models.ForgotPasswordResponse
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpRequest
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpResponse
import com.example.expensetracker.data.models.ResetPasswordRequest
import com.example.expensetracker.data.models.LoginRequest
import com.example.expensetracker.data.models.LoginResponse
import com.example.expensetracker.data.models.RegisterRequest
import com.example.expensetracker.data.models.RegisterResponse
import com.example.expensetracker.data.models.VerifyOtpRequest
import com.example.expensetracker.data.models.VerifyOtpResponse
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
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/v1/auth/forgot-password/send-otp")
    suspend fun forgotPasswordSendOtp(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/v1/auth/forgot-password/verify-otp")
    suspend fun forgotPasswordVerifyOtp(@Body request: ForgotPasswordVerifyOtpRequest): Response<ForgotPasswordVerifyOtpResponse>

    @POST("api/v1/auth/forgot-password/resend-otp")
    suspend fun forgotPasswordResendOtp(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ForgotPasswordResponse>

    @GET("ping")
    suspend fun ping(): Any
}
