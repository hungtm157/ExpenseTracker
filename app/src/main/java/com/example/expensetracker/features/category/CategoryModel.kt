package com.example.expensetracker.features.category

/**
 * Model dữ liệu cho một Danh mục.
 * @param icon       Emoji icon hiển thị trong danh mục
 * @param name       Tên danh mục
 * @param meta       Mô tả phụ (ví dụ: "Mặc định • Đang dùng")
 * @param iconBg     Resource drawable làm nền cho icon
 * @param isEnabled  Trạng thái bật/tắt danh mục
 */
data class CategoryModel(
    val icon: String,
    val name: String,
    val meta: String,
    val iconBg: Int,
    var isEnabled: Boolean = true
)
