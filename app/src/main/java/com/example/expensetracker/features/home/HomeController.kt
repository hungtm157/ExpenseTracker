package com.example.expensetracker.features.home

import com.example.expensetracker.data.models.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HomeController — Xử lý logic nghiệp vụ cho màn hình Trang chủ.
 */
class HomeController(
    private val repository: ExpenseRepository,
    private val listener: HomeListener
) {

    private val scope = CoroutineScope(Dispatchers.Main)

    /** Tải danh sách chi tiêu */
    fun loadExpenses() {
        listener.onLoading(true)
        scope.launch {
            try {
                val expenses = withContext(Dispatchers.IO) {
                    repository.getAllExpenses()
                }
                listener.onLoading(false)
                listener.onExpensesLoaded(expenses)
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Không thể tải dữ liệu")
            }
        }
    }

    /** Xóa một khoản chi tiêu */
    fun deleteExpense(expense: Expense) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) { repository.deleteExpense(expense) }
                loadExpenses()
            } catch (e: Exception) {
                listener.onError(e.message ?: "Không thể xóa")
            }
        }
    }

    /** Interface callbacks cho HomeActivity */
    interface HomeListener {
        fun onExpensesLoaded(expenses: List<Expense>)
        fun onLoading(isLoading: Boolean)
        fun onError(message: String)
    }
}
