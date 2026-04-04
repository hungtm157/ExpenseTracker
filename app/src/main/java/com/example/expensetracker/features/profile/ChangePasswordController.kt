package com.example.expensetracker.features.profile

import android.util.Log
import com.example.expensetracker.data.models.ChangePasswordRequest
import com.example.expensetracker.data.repository.AuthRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

/**
 * ChangePasswordController — Xử lý logic nghiệp vụ cho màn hình Đổi mật khẩu.
 */
class ChangePasswordController(
    private val repository: AuthRepository,
    private val listener: ChangePasswordListener
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "ChangePasswordController"

    /**
     * Thực hiện đổi mật khẩu sau khi đã validate client.
     */
    fun changePassword(current: String, new: String, confirm: String) {
        // 1. Client-side Validation
        if (current.isEmpty()) {
            listener.onFieldError("Vui lòng nhập mật khẩu hiện tại", null, null)
            return
        }
        if (new.length < 6) {
            listener.onFieldError(null, "Mật khẩu phải từ 6 ký tự trở lên", null)
            return
        }
        if (new != confirm) {
            listener.onFieldError(null, null, "Mật khẩu xác nhận không khớp")
            return
        }

        // 2. Gọi API
        Log.d(TAG, "changePassword: Đang gửi yêu cầu đổi mật khẩu")
        listener.onLoading(true)
        val request = ChangePasswordRequest(current, new, confirm)
        
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.changePassword(request)
                }
                listener.onLoading(false)
                
                if (response.isSuccessful) {
                    Log.d(TAG, "changePassword: Thành công")
                    listener.onSuccess(response.body()?.message ?: "Đổi mật khẩu thành công")
                } else {
                    handleError(response.errorBody())
                }
            } catch (e: Exception) {
                Log.e(TAG, "changePassword: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /**
     * Xử lý lỗi từ server (bao gồm lỗi validate trường cụ thể).
     */
    private fun handleError(errorBody: ResponseBody?) {
        try {
            val errorJson = errorBody?.string() ?: ""
            Log.e(TAG, "handleError: $errorJson")
            
            // Parse Error Wrapper
            val errorResponse = Gson().fromJson(errorJson, ChangePasswordErrorResponse::class.java)
            
            if (errorResponse.data != null) {
                // Có lỗi chi tiết từng trường
                listener.onFieldError(
                    errorResponse.data.currentPassword,
                    errorResponse.data.newPassword,
                    errorResponse.data.confirmPassword
                )
            } else {
                // Lỗi chung chung
                listener.onError(errorResponse.message ?: "Đã có lỗi xảy ra")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleError: Lỗi parse error JSON", e)
            listener.onError("Đã có lỗi xảy ra, vui lòng thử lại")
        }
    }

    // --- Các Model phục vụ parse lỗi đặc thù ---
    
    private data class ChangePasswordErrorResponse(
        @SerializedName("message") val message: String?,
        @SerializedName("data") val data: ChangePasswordErrorData?
    )

    private data class ChangePasswordErrorData(
        @SerializedName("currentPassword") val currentPassword: String?,
        @SerializedName("newPassword") val newPassword: String?,
        @SerializedName("confirmPassword") val confirmPassword: String?
    )
}
