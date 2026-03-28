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

    private fun getErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = org.json.JSONObject(errorBody)
                json.optString("message", "Lỗi HTTP: ${response.code()}")
            } else {
                "Lỗi HTTP: ${response.code()}"
            }
        } catch (e: Exception) {
            "Lỗi HTTP: ${response.code()}"
        }
    }

    /** Tải danh sách kế hoạch ngân sách (có hỗ trợ lọc theo ngày) */
    fun loadBudgets(fromDate: String? = null, toDate: String? = null) {
        Log.d(TAG, "loadBudgets: Khởi chạy tải danh sách kế hoạch ngân sách")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getBudgets(fromDate, toDate)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val budgets = response.body()?.data ?: emptyList()
                    Log.d(TAG, "loadBudgets: Thành công, nhận ${budgets.size} kế hoạch")
                    listener.onBudgetsLoaded(budgets)
                } else {
                    val errorMsg = getErrorMessage(response)
                    Log.e(TAG, "loadBudgets: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadBudgets: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Tải chi tiết kế hoạch ngân sách */
    fun loadBudgetDetail(id: Int) {
        Log.d(TAG, "loadBudgetDetail: Tải chi tiết kế hoạch ngân sách ID $id")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getBudgetDetail(id)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val budget = response.body()?.data
                    if (budget != null) {
                        Log.d(TAG, "loadBudgetDetail: Thành công")
                        listener.onBudgetDetailLoaded(budget)
                    } else {
                        listener.onError("Không tìm thấy kế hoạch ngân sách")
                    }
                } else {
                    val errorMsg = getErrorMessage(response)
                    Log.e(TAG, "loadBudgetDetail: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadBudgetDetail: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
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
                    val errorMsg = getErrorMessage(response)
                    Log.e(TAG, "createBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
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
                    val errorMsg = getErrorMessage(response)
                    Log.e(TAG, "completeBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "completeBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
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
                    val errorMsg = getErrorMessage(response)
                    Log.e(TAG, "updateBudget: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateBudget: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }
}
