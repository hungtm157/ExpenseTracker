package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Any?
)
