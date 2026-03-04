package com.example.expensetracker.features.auth.otp

import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.VerifyOtpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * OtpController — Xử lý logic xác thực OTP.
 */
class OtpController(private val listener: OtpListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)

    /**
     * Gọi API verify-otp.
     * @param fullName Họ và tên (truyền từ màn đăng ký)
     * @param email    Email đã đăng ký
     * @param password Mật khẩu đã nhập
     * @param otp      Mã OTP 6 chữ số người dùng nhập
     */
    fun verifyOtp(fullName: String, email: String, password: String, otp: String) {
        if (otp.length != 6) {
            listener.onVerifyFailure("Vui lòng nhập đủ 6 chữ số OTP")
            return
        }

        listener.onVerifyLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.verifyOtp(VerifyOtpRequest(fullName, email, password, otp))
                }

                listener.onVerifyLoading(false)

                when (response.code()) {
                    200 -> listener.onVerifySuccess()
                    400 -> listener.onVerifyFailure("Mã OTP không đúng hoặc đã hết hạn")
                    404 -> listener.onVerifyFailure("Email chưa được đăng ký")
                    else -> listener.onVerifyFailure("Xác thực thất bại (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onVerifyLoading(false)
                listener.onVerifyFailure("Không thể kết nối đến máy chủ")
            }
        }
    }
}
