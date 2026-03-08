package com.example.expensetracker.features.auth.forgotpassword

import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.ResetPasswordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetPasswordController(private val listener: ResetPasswordListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)

    fun resetPassword(token: String, password: String, confirmPassword: String) {
        if (password.isBlank()) {
            listener.onResetFailure("Mật khẩu không được để trống")
            return
        }
        if (password.length < 6) {
            listener.onResetFailure("Mật khẩu phải ít nhất 6 ký tự")
            return
        }
        if (password != confirmPassword) {
            listener.onResetFailure("Mật khẩu xác nhận không khớp")
            return
        }

        listener.onResetLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.resetPassword(ResetPasswordRequest(token, password, confirmPassword))
                }

                listener.onResetLoading(false)
                val body = response.body()

                when (response.code()) {
                    200 -> listener.onResetSuccess(body?.message ?: "Đặt lại mật khẩu thành công")
                    400 -> listener.onResetFailure(body?.message ?: "Token không hợp lệ hoặc đã hết hạn")
                    else -> listener.onResetFailure(body?.message ?: "Lỗi máy chủ (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onResetLoading(false)
                listener.onResetFailure("Không thể kết nối đến máy chủ")
            }
        }
    }
}
