package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

/** Wrapper khớp với cấu trúc server: { status, message, data: UserProfileResponse } */
data class ProfileApiResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserProfileResponse?
)

data class UserProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("fcmToken") val fcmToken: String?,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String
)
