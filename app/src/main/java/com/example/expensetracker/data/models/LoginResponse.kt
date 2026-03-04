package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginData?
)

data class LoginData(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("user") val user: UserInfo
)

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String
)
