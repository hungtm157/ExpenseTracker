package com.example.expensetracker

import android.app.Application
import android.util.Log
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.AppPreferences
import com.example.expensetracker.utils.AdManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * App — Lớp Application khởi tạo toàn bộ ứng dụng.
 * Đây là điểm vào đầu tiên khi app khởi động.
 */
class App : Application() {

    /** Room Database instance dùng chung toàn app */
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    /** SharedPreferences dùng chung toàn app */
    val preferences: AppPreferences by lazy { AppPreferences(this) }

    private val TAG = "App"

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Khởi tạo AdMob
        AdManager.init(this)
        
        // Kiểm tra và cập nhật FCM token nếu cần
        checkAndUpdateFcmToken()
    }

    /**
     * Khi app khởi động, nếu đã đăng nhập (có accessToken),
     * kiểm tra FCM token hiện tại có thay đổi so với token đã lưu không.
     * Nếu thay đổi → gửi token mới lên server.
     */
    private fun checkAndUpdateFcmToken() {
        val prefs = preferences
        if (prefs.authToken.isEmpty()) return  // Chưa đăng nhập → bỏ qua

        FirebaseMessaging.getInstance().token.addOnSuccessListener { currentToken ->
            val savedToken = prefs.fcmToken
            if (currentToken != savedToken) {
                Log.d(TAG, "FCM token thay đổi, cập nhật lên server")
                prefs.fcmToken = currentToken
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val apiService = ApiClient.create(ApiService::class.java)
                        apiService.updateFcmToken(mapOf("fcmToken" to currentToken))
                        Log.d(TAG, "FCM token đã cập nhật lên server thành công")
                    } catch (e: Exception) {
                        Log.e(TAG, "Lỗi cập nhật FCM token lên server", e)
                    }
                }
            } else {
                Log.d(TAG, "FCM token không thay đổi, bỏ qua")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Lỗi lấy FCM token", e)
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
