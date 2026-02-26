package com.example.expensetracker.core.utils

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * ViewUtils — Tiện ích thao tác View.
 */
object ViewUtils {

    /** Hiển thị View */
    fun View.show() {
        visibility = View.VISIBLE
    }

    /** Ẩn View (vẫn chiếm không gian) */
    fun View.hide() {
        visibility = View.INVISIBLE
    }

    /** Ẩn View hoàn toàn (không chiếm không gian) */
    fun View.gone() {
        visibility = View.GONE
    }

    /** Bật / tắt View */
    fun View.enable() { isEnabled = true }
    fun View.disable() { isEnabled = false }
}

/** Toast ngắn từ Fragment */
fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}
