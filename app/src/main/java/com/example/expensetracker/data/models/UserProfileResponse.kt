package com.example.expensetracker.data.models

data class UserProfileResponse(
    val id: Int,
    val email: String,
    val avatar: String?,
    val fullName: String,
    val fcmToken: String?,
    val type: String,
    val status: String
)
