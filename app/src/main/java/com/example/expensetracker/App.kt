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
        
        // Kiểm tra và đồng bộ FCM token ngay khi khởi động
        syncFcmToken()
    }

    /**
     * Đồng bộ FCM token lên server.
     * @param forcedToken Nếu truyền vào (ví dụ từ onNewToken), sẽ dùng token này. 
     * Nếu không, sẽ tự lấy từ FirebaseMessaging.
     */
    fun syncFcmToken(forcedToken: String? = null) {
        val prefs = preferences
        if (prefs.authToken.isEmpty()) {
            // Nếu chưa đăng nhập, không làm gì cả. 
            // Ta sẽ đồng bộ sau khi người dùng đăng nhập thành công.
            return
        }

        if (forcedToken != null) {
            sendTokenToServer(forcedToken)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { currentToken ->
                val savedToken = prefs.fcmToken
                if (currentToken != savedToken) {
                    Log.d(TAG, "FCM token thay đổi hoặc chưa đồng bộ, tiến hành gửi lên server")
                    sendTokenToServer(currentToken)
                } else {
                    Log.d(TAG, "FCM token đã được đồng bộ trước đó, bỏ qua")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Lỗi khi lấy FCM token từ Firebase", e)
            }
        }
    }

    /**
     * Gửi token lên server và CHỈ CẬP NHẬT prefs khi thành công.
     */
    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiClient.create(ApiService::class.java)
                val response = apiService.updateFcmToken(mapOf("fcmToken" to token))
                
                if (response.isSuccessful) {
                    preferences.fcmToken = token
                    Log.d(TAG, "Đã đồng bộ FCM token lên server thành công")
                } else {
                    Log.e(TAG, "Server từ chối cập nhật FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi kết nối khi gửi FCM token lên server", e)
            }
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
