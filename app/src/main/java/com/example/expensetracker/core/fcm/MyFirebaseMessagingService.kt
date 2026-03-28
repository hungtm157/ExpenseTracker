package com.example.expensetracker.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.features.auth.login.LoginActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MyFirebaseMessagingService — xử lý FCM token và push notification.
 * - onNewToken: lưu token mới, gửi lên server nếu đã đăng nhập.
 * - onMessageReceived: hiển thị notification khi app đang foreground.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "expense_tracker_channel"
        private const val CHANNEL_NAME = "Expense Tracker"
    }

    /**
     * Gọi khi FCM cấp token mới (lần đầu cài app hoặc token bị refresh).
     * Lưu token vào AppPreferences và gửi lên server nếu đã đăng nhập.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        val prefs = App.instance.preferences
        prefs.fcmToken = token

        // Nếu đã đăng nhập → gửi token mới lên server
        if (prefs.authToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = ApiClient.create(ApiService::class.java)
                    apiService.updateFcmToken(mapOf("fcmToken" to token))
                    Log.d(TAG, "FCM token đã gửi lên server thành công (onNewToken)")
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi gửi FCM token lên server", e)
                }
            }
        }
    }

    /**
     * Gọi khi nhận được push notification trong lúc app đang foreground.
     * Khi app ở background, hệ thống tự hiển thị notification từ payload.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Expense Tracker"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        Log.d(TAG, "Notification — title: $title | body: $body")
        showNotification(title, body)
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo channel (bắt buộc từ Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo từ Expense Tracker"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Intent mở LoginActivity khi tap vào notification
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_spash)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
