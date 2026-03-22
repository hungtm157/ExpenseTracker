package com.example.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * AppPreferences — Wrapper quản lý SharedPreferences.
 * Lưu các cài đặt nhẹ: token, userId, theme, v.v.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "expense_tracker_prefs"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HAS_SEEN_INTRO = "has_seen_intro"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    var userId: Long
        get() = prefs.getLong(KEY_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_USER_ID, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var authToken: String
        get() = prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var hasSeenIntro: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_INTRO, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEN_INTRO, value).apply()

    var fcmToken: String
        get() = prefs.getString(KEY_FCM_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()

    /** Xóa toàn bộ dữ liệu khi đăng xuất */
    fun clear() = prefs.edit().clear().apply()
}
