package com.example.expensetracker.core.network

import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.models.CategoryListResponse
import com.example.expensetracker.data.models.ErrorResponse
import com.example.expensetracker.data.models.ForgotPasswordRequest
import com.example.expensetracker.data.models.ForgotPasswordResponse
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpRequest
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpResponse
import com.example.expensetracker.data.models.LoginRequest
import com.example.expensetracker.data.models.LoginResponse
import com.example.expensetracker.data.models.RegisterRequest
import com.example.expensetracker.data.models.RegisterResponse
import com.example.expensetracker.data.models.ResetPasswordRequest
import com.example.expensetracker.data.models.VerifyOtpRequest
import com.example.expensetracker.data.models.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

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

    // ─── Categories ──────────────────────────────────────────────────────────

    @GET("api/v1/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("type") type: String
    ): Response<CategoryListResponse>

    @retrofit2.http.Multipart
    @POST("api/v1/categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @retrofit2.http.Part("name") name: okhttp3.RequestBody,
        @retrofit2.http.Part("type") type: okhttp3.RequestBody,
        @retrofit2.http.Part icon: okhttp3.MultipartBody.Part?
    ): Response<CategoryItem>

    @retrofit2.http.Multipart
    @PATCH("api/v1/categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int,
        @retrofit2.http.Part("name") name: okhttp3.RequestBody?,
        @retrofit2.http.Part("type") type: okhttp3.RequestBody?,
        @retrofit2.http.Part("status") status: okhttp3.RequestBody?,
        @retrofit2.http.Part icon: okhttp3.MultipartBody.Part?
    ): Response<CategoryItem>

    @DELETE("api/v1/categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int
    ): Response<ErrorResponse>

    @GET("ping")
    suspend fun ping(): Any
}
