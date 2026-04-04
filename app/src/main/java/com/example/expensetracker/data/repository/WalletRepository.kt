package com.example.expensetracker.data.repository

import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.features.transaction.TransactionListResponse
import com.example.expensetracker.features.wallet.*
import retrofit2.Response

/**
 * WalletRepository — Lớp trung gian xử lý dữ liệu Ví từ API.
 */
class WalletRepository(private val apiService: ApiService) {

    /** Lấy danh sách ví */
    suspend fun getWallets(
        page: Int = 1,
        limit: Int = 20,
        sort: String? = null,
        search: String? = null,
        type: String? = null
    ): Response<WalletListResponse> {
        return apiService.getWallets(page, limit, sort, search, type)
    }

    /** Tạo ví mới */
    suspend fun createWallet(request: WalletCreateRequest): Response<WalletModel> {
        return apiService.createWallet(request)
    }

    /** Cập nhật ví */
    suspend fun updateWallet(id: Int, request: WalletUpdateRequest): Response<WalletModel> {
        return apiService.updateWallet(id, request)
    }

    /** Xóa ví */
    suspend fun deleteWallet(id: Int): Response<WalletDeleteResponse> {
        return apiService.deleteWallet(id)
    }

    /** Lấy số lượng giao dịch của ví */
    suspend fun getTransactionCount(walletId: Int): Response<TransactionListResponse> {
        return apiService.getTransactions(walletId = walletId, limit = 1)
    }
}
