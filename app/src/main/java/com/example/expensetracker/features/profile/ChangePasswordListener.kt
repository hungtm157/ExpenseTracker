package com.example.expensetracker.features.profile

/**
 * ChangePasswordListener — Interface định nghĩa các callback cho màn hình Đổi mật khẩu.
 */
interface ChangePasswordListener {
    fun onLoading(isLoading: Boolean)
    fun onSuccess(message: String)
    fun onError(message: String)
    
    /**
     * Callback khi có lỗi validate cụ thể cho từng trường từ Server trả về.
     */
    fun onFieldError(currentError: String?, newError: String?, confirmError: String?)
}
