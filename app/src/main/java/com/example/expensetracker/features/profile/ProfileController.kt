package com.example.expensetracker.features.profile

import com.example.expensetracker.data.local.AppPreferences

/**
 * ProfileController — Xử lý logic nghiệp vụ cho màn hình Cá nhân.
 */
class ProfileController(
    private val prefs: AppPreferences,
    private val listener: ProfileListener
) {

    /** Tải thông tin người dùng từ SharedPreferences */
    fun loadProfile() {
        val name = prefs.userName.ifBlank { "Người dùng" }
        listener.onProfileLoaded(name)
    }

    /** Đăng xuất */
    fun logout() {
        prefs.clear()
        listener.onLoggedOut()
    }

    interface ProfileListener {
        fun onProfileLoaded(name: String)
        fun onLoggedOut()
    }
}
