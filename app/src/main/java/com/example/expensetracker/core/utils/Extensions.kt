package com.example.expensetracker.core.utils

import android.content.Context
import android.widget.Toast

// ───── String Extensions ─────────────────────────────────────────────────────

/** Kiểm tra chuỗi rỗng hoặc toàn khoảng trắng */
fun String?.isNullOrBlankCustom(): Boolean = this.isNullOrBlank()

/** Chuyển đổi chuỗi sang Double, trả về 0.0 nếu lỗi */
fun String.toDoubleOrZero(): Double = this.toDoubleOrNull() ?: 0.0

/** Chuyển đổi chuỗi sang Int, trả về 0 nếu lỗi */
fun String.toIntOrZero(): Int = this.toIntOrNull() ?: 0

// ───── Context Extensions ─────────────────────────────────────────────────────

/** Hiển thị Toast ngắn */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** Hiển thị Toast dài */
fun Context.toastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ───── Number Extensions ──────────────────────────────────────────────────────

/** Định dạng số tiền (VND) */
fun Double.toCurrencyString(): String = "%,.0f VNĐ".format(this)

/** Định dạng số tiền ngắn gọn */
fun Long.toCurrencyString(): String = this.toDouble().toCurrencyString()
