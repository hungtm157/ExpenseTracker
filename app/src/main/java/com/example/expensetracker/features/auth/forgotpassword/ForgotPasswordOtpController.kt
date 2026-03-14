package com.example.expensetracker.features.auth.forgotpassword

import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.ForgotPasswordRequest
import com.example.expensetracker.data.models.ForgotPasswordVerifyOtpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordOtpController(private val listener: ForgotPasswordOtpListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)

    fun verifyOtp(email: String, otp: String) {
        if (otp.length != 6) {
            listener.onVerifyFailure("Vui lòng nhập đủ 6 chữ số OTP")
            return
        }

        listener.onVerifyLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.forgotPasswordVerifyOtp(ForgotPasswordVerifyOtpRequest(email, otp))
                }

                listener.onVerifyLoading(false)
                val body = response.body()

                when (response.code()) {
                    200 -> {
                        val token = body?.data?.forgotPasswordToken
                        if (token != null) {
                            listener.onVerifySuccess(body.message, token)
                        } else {
                            listener.onVerifyFailure("Không nhận được token từ máy chủ")
                        }
                    }
                    400 -> listener.onVerifyFailure(body?.message ?: "Mã OTP không đúng hoặc đã hết hạn")
                    else -> listener.onVerifyFailure(body?.message ?: "Xác thực thất bại (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onVerifyLoading(false)
                listener.onVerifyFailure("Không thể kết nối đến máy chủ")
            }
        }
    }

    fun resendOtp(email: String) {
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.forgotPasswordResendOtp(ForgotPasswordRequest(email))
                }
                val body = response.body()
                if (response.isSuccessful) {
                    listener.onResendSuccess(body?.message ?: "Đã gửi lại mã OTP")
                } else {
                    listener.onResendFailure(body?.message ?: "Gửi lại mã thất bại")
                }
            } catch (e: Exception) {
                listener.onResendFailure("Không thể kết nối đến máy chủ")
            }
        }
    }
}
