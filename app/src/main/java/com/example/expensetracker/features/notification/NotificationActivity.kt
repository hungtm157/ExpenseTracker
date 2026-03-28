package com.example.expensetracker.features.notification

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NotificationActivity — Màn hình xem danh sách thông báo.
 */
class NotificationActivity : BaseActivity(R.layout.activity_notification) {

    private lateinit var btnBack: ImageView
    private lateinit var btnDeleteAll: ImageView
    private lateinit var recyclerNotifications: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: NotificationAdapter
    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)
    private val TAG = "NotificationActivity"

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnDeleteAll = findViewById(R.id.btnDeleteAll)
        recyclerNotifications = findViewById(R.id.recyclerNotifications)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        progressBar = findViewById(R.id.progressBar)

        adapter = NotificationAdapter(emptyList()) { notification ->
            // Mark as read khi click
            markAsRead(notification)
        }

        recyclerNotifications.layoutManager = LinearLayoutManager(this)
        recyclerNotifications.adapter = adapter

        loadNotifications()
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        btnDeleteAll.setOnClickListener {
            deleteAllNotifications()
        }
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getNotifications()
                }
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val notifications = response.body()?.data ?: emptyList()
                    adapter.updateData(notifications)

                    if (notifications.isEmpty()) {
                        recyclerNotifications.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    } else {
                        recyclerNotifications.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@NotificationActivity, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Lỗi kết nối", e)
                Toast.makeText(this@NotificationActivity, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markAsRead(notification: NotificationModel) {
        if (notification.isRead) return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    apiService.markNotificationRead(notification.id)
                }
                // Reload to update UI
                loadNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi đánh dấu đã đọc", e)
            }
        }
    }

    private fun deleteAllNotifications() {
        scope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                withContext(Dispatchers.IO) {
                    apiService.deleteAllNotifications()
                }
                Toast.makeText(this@NotificationActivity, "Đã xóa tất cả thông báo", Toast.LENGTH_SHORT).show()
                loadNotifications()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Lỗi xóa thông báo", e)
                Toast.makeText(this@NotificationActivity, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
