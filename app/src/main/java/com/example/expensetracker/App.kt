package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.AppPreferences

/**
 * App — Lớp Application khởi tạo toàn bộ ứng dụng.
 * Đây là điểm vào đầu tiên khi app khởi động.
 */
class App : Application() {

    /** Room Database instance dùng chung toàn app */
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    /** SharedPreferences dùng chung toàn app */
    val preferences: AppPreferences by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // TODO: Khởi tạo các thư viện khác (Firebase, Timber, v.v.) ở đây
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
