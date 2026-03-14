package com.example.expensetracker.features.plan

import android.util.Log
import com.example.expensetracker.data.repository.BudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * BudgetController — Xử lý logic nghiệp vụ cho màn hình Kế hoạch ngân sách.
 */
class BudgetController(
    private val repository: BudgetRepository,
    private val listener: BudgetListener
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "BudgetController"

    /** Tải danh sách kế hoạch ngân sách */
    fun loadBudgets() {
        Log.d(TAG, "loadBudgets: Khởi chạy tải danh sách kế hoạch ngân sách")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getBudgets()
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val budgets = response.body()?.data ?: emptyList()
                    Log.d(TAG, "loadBudgets: Thành công, nhận ${budgets.size} kế hoạch")
                    listener.onBudgetsLoaded(budgets)
                } else {
                    val errorMsg = "Lỗi HTTP: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "loadBudgets: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadBudgets: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Tạo kế hoạch ngân sách mới */
    fun createBudget(request: BudgetCreateRequest) {
        Log.d(TAG, "createBudget: Đang tạo kế hoạch ngân sách mới")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createBudget(request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "createBudget: Tạo thành công ID ${it.id}")
                        listener.onBudgetCreated(it)
                    }
                } else {
                    val errorMsg = "Lỗi tạo kế hoạch: ${response.code()}"
                    Log.e(TAG, "createBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi kết nối")
            }
        }
    }

    /** Hoàn thành và chốt kế hoạch ngân sách */
    fun completeBudget(id: Int) {
        Log.d(TAG, "completeBudget: Đang chốt kế hoạch ngân sách ID $id")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.completeBudget(id)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "completeBudget: Chốt thành công kế hoạch ID $id")
                        listener.onBudgetCompleted(it)
                    }
                } else {
                    val errorMsg = "Lỗi chốt kế hoạch: ${response.code()}"
                    Log.e(TAG, "completeBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "completeBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi kết nối")
            }
        }
    }

    /** Cập nhật kế hoạch ngân sách */
    fun updateBudget(id: Int, request: BudgetUpdateRequest) {
        Log.d(TAG, "updateBudget: Đang cập nhật kế hoạch ngân sách ID $id")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateBudget(id, request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "updateBudget: Cập nhật thành công kế hoạch ID $id")
                        listener.onBudgetUpdated(it)
                    }
                } else {
                    val errorMsg = "Lỗi cập nhật kế hoạch: ${response.code()}"
                    Log.e(TAG, "updateBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi kết nối")
            }
        }
    }
}
