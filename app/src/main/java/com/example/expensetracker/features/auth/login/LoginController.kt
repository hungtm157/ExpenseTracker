package com.example.expensetracker.features.auth.login

import com.example.expensetracker.App
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LoginController — Xử lý logic nghiệp vụ cho màn hình Đăng nhập.
 * Nhận dữ liệu từ View, gọi API thực tế, lưu token và thông báo kết quả qua LoginListener.
 */
class LoginController(private val listener: LoginListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)
    private val prefs = App.instance.preferences

    /**
     * @param email    Email người dùng
     * @param password Mật khẩu
     */
    fun login(email: String, password: String) {
        if (!validateInput(email, password)) return

        listener.onLoginLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.login(LoginRequest(email, password))
                }

                listener.onLoginLoading(false)

                when (response.code()) {
                    200 -> {
                        val body = response.body()
                        val token = body?.data?.accessToken
                        val user = body?.data?.user

                        if (token != null && user != null) {
                            // Lưu token và thông tin đăng nhập
                            prefs.authToken = token
                            prefs.isLoggedIn = true
                            prefs.userName = user.fullName
                            prefs.userId = user.id.toLong()
                            listener.onLoginSuccess()
                        } else {
                            listener.onLoginFailure("Phản hồi từ máy chủ không hợp lệ")
                        }
                    }
                    400 -> listener.onLoginFailure("Sai thông tin đăng nhập")
                    403 -> listener.onLoginFailure("Tài khoản bị cấm")
                    else -> listener.onLoginFailure("Lỗi máy chủ (${response.code()})")
                }
            } catch (e: Exception) {
                listener.onLoginLoading(false)
                listener.onLoginFailure("Không thể kết nối đến máy chủ")
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
}
