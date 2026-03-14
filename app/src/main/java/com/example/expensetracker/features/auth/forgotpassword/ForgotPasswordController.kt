package com.example.expensetracker.features.auth.forgotpassword

import android.util.Patterns
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.ForgotPasswordRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordController(private val listener: ForgotPasswordListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)

    fun sendOtp(email: String) {
        if (email.isBlank()) {
            listener.onSendOtpFailure("Email không được để trống")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            listener.onSendOtpFailure("Email không hợp lệ")
            return
        }

        listener.onSendOtpLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.forgotPasswordSendOtp(ForgotPasswordRequest(email))
                }

                listener.onSendOtpLoading(false)

                val body = response.body()
                when (response.code()) {
                    200 -> listener.onSendOtpSuccess(body?.message ?: "Đã gửi mã OTP, vui lòng kiểm tra email")
                    400 -> listener.onSendOtpFailure(body?.message ?: "Email không tồn tại trong hệ thống")
                    else -> listener.onSendOtpFailure(body?.message ?: "Lỗi máy chủ (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onSendOtpLoading(false)
                listener.onSendOtpFailure("Không thể kết nối đến máy chủ")
            }
        }
    }
}
