package com.example.expensetracker.features.wallet

import android.util.Log
import com.example.expensetracker.data.repository.WalletRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * WalletController — Xử lý logic nghiệp vụ cho màn hình Quản lý ví.
 */
class WalletController(
    private val repository: WalletRepository,
    private val listener: WalletListener
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "WalletController"

    /** Tải danh sách ví */
    fun loadWallets() {
        Log.d(TAG, "loadWallets: Khởi chạy tải danh sách ví")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getWallets()
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val wallets = response.body()?.data?.items ?: emptyList()
                    Log.d(TAG, "loadWallets: Thành công, nhận ${wallets.size} ví")
                    listener.onWalletsLoaded(wallets)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "loadWallets: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadWallets: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Tạo ví mới */
    fun createWallet(name: String, type: WalletType, balance: Double, currency: String) {
        Log.d(TAG, "createWallet: Đang tạo ví '$name'")
        val request = WalletCreateRequest(name, type, balance, currency)
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createWallet(request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { 
                        Log.d(TAG, "createWallet: Tạo thành công ID ${it.id}")
                        listener.onWalletCreated(it) 
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "createWallet: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createWallet: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Cập nhật ví */
    fun updateWallet(id: Int, request: WalletUpdateRequest) {
        Log.d(TAG, "updateWallet: Đang cập nhật ví ID $id")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateWallet(id, request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { 
                        Log.d(TAG, "updateWallet: Cập nhật thành công ví ID $id")
                        listener.onWalletUpdated(it) 
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "updateWallet: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateWallet: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Xóa ví */
    fun deleteWallet(id: Int) {
        Log.d(TAG, "deleteWallet: Đang xóa ví ID $id")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.deleteWallet(id)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    Log.d(TAG, "deleteWallet: Xóa thành công ví ID $id")
                    listener.onWalletDeleted(response.body()?.message ?: "Xóa thành công")
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "deleteWallet: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteWallet: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }
    /** Kiểm tra số lượng giao dịch trước khi xóa */
    fun checkTransactionCountBeforeDelete(wallet: WalletModel) {
        Log.d(TAG, "checkTransactionCountBeforeDelete: Đang kiểm tra giao dịch của ví ID ${wallet.id}")
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getTransactionCount(wallet.id)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val total = response.body()?.data?.total ?: 0
                    listener.onTransactionCountReceived(total, wallet)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /**
     * Helper trích xuất thông báo lỗi từ JSON body của API.
     */
    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?): String {
        return try {
            val errorJson = errorBody?.string()
            val errorResponse = Gson().fromJson(errorJson, com.example.expensetracker.data.models.ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "Đã có lỗi xảy ra, vui lòng thử lại"
        }
    }
}
