package com.example.expensetracker.data.models

/**
 * Expense — Model đại diện cho một khoản chi tiêu.
 */
data class Expense(
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),   // timestamp (ms)
    val userId: Long = 0L
)
