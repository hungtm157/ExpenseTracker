package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class VerifyOtpRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("otp") val otp: String
)

data class RegisterResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any?
)

data class VerifyOtpResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: VerifyOtpData?
)

data class VerifyOtpData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("user") val user: VerifyOtpUser
)

data class VerifyOtpUser(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String
)
