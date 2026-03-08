package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ForgotPasswordVerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class ForgotPasswordResendOtpRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("forgotPasswordToken") val forgotPasswordToken: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)

data class ForgotPasswordResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any?
)

data class ForgotPasswordVerifyOtpResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ForgotPasswordVerifyOtpData?
)

data class ForgotPasswordVerifyOtpData(
    @SerializedName("forgotPasswordToken") val forgotPasswordToken: String
)
