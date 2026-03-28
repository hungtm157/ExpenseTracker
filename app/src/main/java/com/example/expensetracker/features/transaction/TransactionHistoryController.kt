package com.example.expensetracker.features.transaction

import android.content.Context
import com.example.expensetracker.App
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller để xử lý logic lấy danh sách Lịch sử giao dịch.
 */
class TransactionHistoryController(
    private val view: TransactionHistoryListener,
    private val context: Context
) {
    private val transactionRepository: TransactionRepository

    init {
        val apiService = ApiClient.create(ApiService::class.java)
        transactionRepository = TransactionRepository(apiService)
    }

    /** Lấy danh sách giao dịch từ API */
    fun fetchTransactions(type: String? = null, fromDate: String? = null, toDate: String? = null) {
        val token = App.instance.preferences.authToken

        if (token.isNullOrEmpty()) {
            view.onError("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn.")
            return
        }

        view.onLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch full data with limit sufficiently high for filtering client-side
                val response = transactionRepository.getTransactions(
                    token = token,
                    page = 1,
                    type = type,
                    fromDate = fromDate,
                    toDate = toDate
                )

                withContext(Dispatchers.Main) {
                    view.onLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()?.data
                        if (data != null) {
                            view.onTransactionsLoaded(data.items)
                        } else {
                            view.onError("Lỗi cấu trúc dữ liệu trả về.")
                        }
                    } else {
                        view.onError("Lấy danh sách giao dịch thất bại: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.onLoading(false)
                    view.onError("Không thể kết nối đến máy chủ")
                }
            }
        }
    }

    /** Xóa giao dịch theo ID */
    fun deleteTransaction(transactionId: Int) {
        val token = App.instance.preferences.authToken

        if (token.isNullOrEmpty()) {
            view.onError("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn.")
            return
        }

        view.onLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = transactionRepository.deleteTransaction(transactionId)
                withContext(Dispatchers.Main) {
                    view.onLoading(false)
                    if (response.isSuccessful) {
                        view.onDeleteSuccess(transactionId)
                    } else {
                        view.onError("Xóa giao dịch thất bại: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.onLoading(false)
                    view.onError("Không thể kết nối đến máy chủ")
                }
            }
        }
    }
}
