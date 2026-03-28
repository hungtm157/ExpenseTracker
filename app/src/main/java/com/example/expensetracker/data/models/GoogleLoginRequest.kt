package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

/**
 * GoogleLoginRequest — Body cho API /api/v1/auth/google
 */
data class GoogleLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("syncId") val syncId: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("avatar") val avatar: String?
)
