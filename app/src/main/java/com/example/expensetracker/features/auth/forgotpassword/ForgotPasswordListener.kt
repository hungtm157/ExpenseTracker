package com.example.expensetracker.features.auth.forgotpassword

interface ForgotPasswordListener {
    fun onSendOtpSuccess(message: String)
    fun onSendOtpFailure(errorMessage: String)
    fun onSendOtpLoading(isLoading: Boolean)
}
