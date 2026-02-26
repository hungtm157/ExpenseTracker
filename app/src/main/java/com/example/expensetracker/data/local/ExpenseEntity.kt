package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ExpenseEntity — Room Entity ánh xạ sang bảng "expenses" trong cơ sở dữ liệu.
 * Khác với data class Expense (domain model), Entity này dùng riêng cho Room.
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val userId: Long = 0L
)
