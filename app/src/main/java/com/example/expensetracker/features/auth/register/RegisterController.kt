package com.example.expensetracker.features.auth.register

import android.util.Patterns
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * RegisterController — Gọi API send-otp để bắt đầu đăng ký.
 * Sau khi gửi OTP thành công, View sẽ navigate sang OtpActivity.
 */
class RegisterController(private val listener: RegisterListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (!validateInput(name, email, password, confirmPassword)) return

        listener.onRegisterLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.sendOtp(RegisterRequest(name, email, password))
                }

                listener.onRegisterLoading(false)

                when (response.code()) {
                    200 -> listener.onRegisterSuccess()
                    400 -> listener.onRegisterFailure("Email đã được đăng ký hoặc thông tin không hợp lệ")
                    else -> listener.onRegisterFailure("Lỗi máy chủ (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onRegisterLoading(false)
                listener.onRegisterFailure("Không thể kết nối đến máy chủ")
            }
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isBlank()) {
            listener.onRegisterFailure("Họ và tên không được để trống")
            return false
        }
        if (email.isBlank()) {
            listener.onRegisterFailure("Email không được để trống")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            listener.onRegisterFailure("Email không hợp lệ")
            return false
        }
        if (password.isBlank()) {
            listener.onRegisterFailure("Mật khẩu không được để trống")
            return false
        }
        if (password.length < 6) {
            listener.onRegisterFailure("Mật khẩu phải ít nhất 6 ký tự")
            return false
        }
        if (password != confirmPassword) {
            listener.onRegisterFailure("Mật khẩu xác nhận không khớp")
            return false
        }
        return true
    }
}
