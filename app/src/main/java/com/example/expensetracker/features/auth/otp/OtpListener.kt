package com.example.expensetracker.features.auth.otp

/**
 * OtpListener — Interface giao tiếp từ OtpController về View.
 */
interface OtpListener {
    fun onVerifySuccess()
    fun onVerifyFailure(errorMessage: String)
    fun onVerifyLoading(isLoading: Boolean)
}
