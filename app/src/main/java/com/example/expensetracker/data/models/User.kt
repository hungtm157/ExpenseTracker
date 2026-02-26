package com.example.expensetracker.data.models

/**
 * User — Model đại diện cho người dùng.
 */
data class User(
    val id: Long = 0L,
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
