package com.example.expensetracker.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DateUtils — Tiện ích xử lý ngày/giờ.
 */
object DateUtils {

    private const val DEFAULT_FORMAT = "dd/MM/yyyy"
    private const val FULL_FORMAT    = "dd/MM/yyyy HH:mm"

    /** Định dạng timestamp (ms) thành chuỗi ngày dd/MM/yyyy */
    fun formatDate(timestamp: Long, pattern: String = DEFAULT_FORMAT): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /** Định dạng timestamp thành chuỗi ngày giờ đầy đủ */
    fun formatDateTime(timestamp: Long): String = formatDate(timestamp, FULL_FORMAT)

    /** Chuyển chuỗi ngày (dd/MM/yyyy) thành timestamp (ms), trả về null nếu lỗi */
    fun parseDate(dateString: String, pattern: String = DEFAULT_FORMAT): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    /** Lấy timestamp hiện tại */
    fun now(): Long = System.currentTimeMillis()
}
