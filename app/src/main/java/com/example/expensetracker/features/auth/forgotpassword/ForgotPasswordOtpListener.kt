package com.example.expensetracker.features.auth.forgotpassword

interface ForgotPasswordOtpListener {
    fun onVerifySuccess(message: String, token: String)
    fun onVerifyFailure(errorMessage: String)
    fun onVerifyLoading(isLoading: Boolean)
    fun onResendSuccess(message: String)
    fun onResendFailure(errorMessage: String)
}
