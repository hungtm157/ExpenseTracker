package com.example.expensetracker.features.auth.login

/**
 * LoginListener — Interface giao tiếp từ Controller về View.
 * View (LoginActivity) implement interface này để nhận kết quả từ controller.
 */
interface LoginListener {
    /** Gọi khi đăng nhập thành công */
    fun onLoginSuccess()

    /** Gọi khi đăng nhập thất bại */
    fun onLoginFailure(errorMessage: String)

    /** Gọi khi đang loading */
    fun onLoginLoading(isLoading: Boolean)
}
