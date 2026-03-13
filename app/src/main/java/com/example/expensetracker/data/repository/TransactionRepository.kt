package com.example.expensetracker.data.repository

import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.features.transaction.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

/**
 * TransactionRepository — Lớp trung gian xử lý dữ liệu Giao dịch từ API.
 */
class TransactionRepository(private val apiService: ApiService) {

    /** Lấy danh sách giao dịch */
    suspend fun getTransactions(
        page: Int = 1,
        limit: Int = 20,
        sort: String? = "date_desc",
        search: String? = null,
        type: String? = null,
        walletId: Int? = null,
        categoryId: Int? = null,
        fromDate: String? = null,
        toDate: String? = null,
        source: String? = null
    ): Response<TransactionListResponse> {
        return apiService.getTransactions(
            page, limit, sort, search, type, walletId, categoryId, fromDate, toDate, source
        )
    }

    /** Tạo giao dịch mới */
    suspend fun createTransaction(
        walletId: RequestBody,
        categoryId: RequestBody,
        amount: RequestBody,
        transactionDate: RequestBody,
        note: RequestBody? = null,
        currency: RequestBody? = null,
        receiptImage: MultipartBody.Part? = null
    ): Response<TransactionModel> {
        return apiService.createTransaction(
            walletId, categoryId, amount, transactionDate, note, currency, receiptImage
        )
    }

    /** Quét OCR hóa đơn */
    suspend fun ocrScan(
        walletId: RequestBody,
        categoryId: RequestBody,
        receiptImage: MultipartBody.Part
    ): Response<TransactionModel> {
        return apiService.ocrScan(walletId, categoryId, receiptImage)
    }

    /** Cập nhật giao dịch */
    suspend fun updateTransaction(
        id: Int,
        walletId: RequestBody? = null,
        categoryId: RequestBody? = null,
        amount: RequestBody? = null,
        transactionDate: RequestBody? = null,
        note: RequestBody? = null,
        currency: RequestBody? = null,
        status: RequestBody? = null,
        receiptImage: MultipartBody.Part? = null
    ): Response<TransactionModel> {
        return apiService.updateTransaction(
            id, walletId, categoryId, amount, transactionDate, note, currency, status, receiptImage
        )
    }

    /** Xóa giao dịch */
    suspend fun deleteTransaction(id: Int): Response<Void> {
        return apiService.deleteTransaction(id)
    }
}
