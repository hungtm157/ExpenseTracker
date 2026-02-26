package com.example.expensetracker.features.auth.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * LoginController — Xử lý logic nghiệp vụ cho màn hình Đăng nhập.
 * Nhận dữ liệu từ View, xử lý và thông báo kết quả qua LoginListener.
 */
class LoginController(private val listener: LoginListener) {

    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Thực hiện đăng nhập.
     * @param email    Email người dùng
     * @param password Mật khẩu
     */
    fun login(email: String, password: String) {
        if (!validateInput(email, password)) return

        listener.onLoginLoading(true)

        scope.launch {
            try {
                // TODO: Thay bằng gọi Repository / API thực tế
                // Ví dụ: val result = userRepository.login(email, password)
                simulateLogin(email, password)
            } catch (e: Exception) {
                listener.onLoginLoading(false)
                listener.onLoginFailure(e.message ?: "Đăng nhập thất bại")
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank()) {
            listener.onLoginFailure("Email không được để trống")
            return false
        }
        if (password.isBlank()) {
            listener.onLoginFailure("Mật khẩu không được để trống")
            return false
        }
        if (password.length < 6) {
            listener.onLoginFailure("Mật khẩu phải ít nhất 6 ký tự")
            return false
        }
        return true
    }

    /** Giả lập login — Xóa khi tích hợp API thật */
    private fun simulateLogin(email: String, password: String) {
        listener.onLoginLoading(false)
        if (email == "test@example.com" && password == "123456") {
            listener.onLoginSuccess()
        } else {
            listener.onLoginFailure("Email hoặc mật khẩu không đúng")
        }
    }
}
