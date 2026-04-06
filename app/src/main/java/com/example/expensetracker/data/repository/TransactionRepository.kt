package com.example.expensetracker.data.repository

import android.util.Log
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
        token: String,
        page: Int = 1,
        limit: Int = Int.MAX_VALUE,
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

    /** Tạo giao dịch mới (JSON) */
    suspend fun createTransaction(
        request: TransactionCreateRequest
    ): Response<TransactionModel> {
        return apiService.createTransaction(request)
    }

    /** Quét hóa đơn — API mới chỉ cần ảnh */
    suspend fun scanInvoice(
        imagePart: MultipartBody.Part
    ): Response<com.example.expensetracker.data.models.ScanInvoiceApiResponse> {
        return apiService.scanInvoice(imagePart)
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
        Log.d("updateTransaction", "Body: ${id} - ${walletId} - ${categoryId}")
        return apiService.updateTransaction(
            id, walletId, categoryId, amount, transactionDate, note, currency, status, receiptImage
        )
    }

    /** Xóa giao dịch */
    suspend fun deleteTransaction(id: Int): Response<Void> {
        return apiService.deleteTransaction(id)
    }
}
