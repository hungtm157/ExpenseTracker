package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.models.Expense

/**
 * ExpenseRepository — Lớp trung gian giữa Controller và Data Source (Room).
 * Controller chỉ tương tác với Repository, không cần biết dữ liệu đến từ đâu.
 */
class ExpenseRepository(private val db: AppDatabase) {

    private val dao = db.expenseDao()

    /** Thêm chi tiêu mới, trả về id được tạo */
    suspend fun addExpense(expense: Expense): Long {
        return dao.insert(expense.toEntity())
    }

    /** Cập nhật chi tiêu */
    suspend fun updateExpense(expense: Expense) {
        dao.update(expense.toEntity())
    }

    /** Xóa chi tiêu */
    suspend fun deleteExpense(expense: Expense) {
        dao.delete(expense.toEntity())
    }

    /** Lấy tất cả chi tiêu */
    suspend fun getAllExpenses(): List<Expense> {
        return dao.getAllExpenses().map { it.toDomain() }
    }

    /** Lấy chi tiêu theo id */
    suspend fun getExpenseById(id: Long): Expense? {
        return dao.getExpenseById(id)?.toDomain()
    }

    /** Tổng số tiền chi tiêu */
    suspend fun getTotalAmount(): Double {
        return dao.getTotalAmount() ?: 0.0
    }

    // ─── Mapper helpers ───────────────────────────────────────────────────────

    private fun Expense.toEntity() = ExpenseEntity(
        id       = id,
        title    = title,
        amount   = amount,
        category = category,
        note     = note,
        date     = date,
        userId   = userId
    )

    private fun ExpenseEntity.toDomain() = Expense(
        id       = id,
        title    = title,
        amount   = amount,
        category = category,
        note     = note,
        date     = date,
        userId   = userId
    )
}
