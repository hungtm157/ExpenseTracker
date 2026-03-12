package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

// ─── Response wrapper ──────────────────────────────────────────────────────

data class CategoryListResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CategoryPageData?
)

data class CategoryPageData(
    @SerializedName("items") val items: List<CategoryItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

// ─── Category item từ API ──────────────────────────────────────────────────

data class CategoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int?,          // null = do admin tạo (Mặc định)
    @SerializedName("name") val name: String,
    @SerializedName("name_normalized") val nameNormalized: String,
    @SerializedName("type") val type: String,             // "EXPENSE" | "INCOME"
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("status") val status: String,         // "ACTIVATE" | "DEACTIVATE"
    @SerializedName("createdAt") val createdAt: String
) {
    /** true nếu danh mục do admin tạo sẵn */
    val isDefault: Boolean get() = userId == null

    /** true nếu đang hoạt động */
    val isActive: Boolean get() = status == "ACTIVATE"

    /** Text hiển thị bên dưới tên: "Mặc định • Đang dùng", "Đang dùng", "Không dùng"... */
    val metaText: String get() {
        val statusLabel = if (isActive) "Đang dùng" else "Không dùng"
        return if (isDefault) "Mặc định • $statusLabel" else statusLabel
    }
}
