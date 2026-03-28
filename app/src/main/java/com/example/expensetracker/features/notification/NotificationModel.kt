package com.example.expensetracker.features.notification

import com.google.gson.annotations.SerializedName

/**
 * NotificationModel — Model đại diện cho một thông báo.
 */
data class NotificationModel(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String, // WARNING, REMINDER, SYSTEM
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("status") val status: String, // ACTIVE, DELETED
    @SerializedName("created_at") val createdAt: String
)

/**
 * NotificationListResponse — Wrapper cho danh sách thông báo trả về từ API.
 */
data class NotificationListResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<NotificationModel>?
)
