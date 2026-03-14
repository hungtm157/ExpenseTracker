package com.example.expensetracker.features.auth.forgotpassword

interface ResetPasswordListener {
    fun onResetSuccess(message: String)
    fun onResetFailure(errorMessage: String)
    fun onResetLoading(isLoading: Boolean)
}
