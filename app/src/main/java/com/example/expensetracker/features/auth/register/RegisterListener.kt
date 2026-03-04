package com.example.expensetracker.features.auth.register

/**
 * RegisterListener — Interface giao tiếp từ Controller về View.
 * View (RegisterActivity) implement interface này để nhận kết quả từ controller.
 */
interface RegisterListener {
    /** Gọi khi đăng ký thành công */
    fun onRegisterSuccess()

    /** Gọi khi đăng ký thất bại */
    fun onRegisterFailure(errorMessage: String)

    /** Gọi khi đang loading */
    fun onRegisterLoading(isLoading: Boolean)
}
