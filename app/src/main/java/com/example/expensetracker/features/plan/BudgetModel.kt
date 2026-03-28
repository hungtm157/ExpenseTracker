package com.example.expensetracker.features.plan

import com.example.expensetracker.data.models.CategoryItem
import com.google.gson.annotations.SerializedName

/**
 * BudgetModel — Model đại diện cho một kế hoạch ngân sách.
 */
data class BudgetModel(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("amount_limit") val amountLimit: Double,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("status") val status: BudgetStatus,
    @SerializedName("final_spent_amount") val finalSpentAmount: Double?,
    @SerializedName("result_status") val resultStatus: BudgetResultStatus?,
    @SerializedName("is_alert_enabled") val isAlertEnabled: Boolean,
    @SerializedName("alert_threshold") val alertThreshold: Double,
    @SerializedName("completion_date") val completionDate: String?,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("category") val category: CategoryItem? = null,
    @SerializedName("current_spent") val currentSpent: Double? = null,
    @SerializedName("remaining_budget") val remainingBudget: Double? = null,
    @SerializedName("percentage_used") val percentageUsed: Double? = null
)

enum class BudgetStatus {
    @SerializedName("ACTIVE") ACTIVE,
    @SerializedName("COMPLETED") COMPLETED,
    @SerializedName("CANCELLED") CANCELLED
}

enum class BudgetResultStatus {
    @SerializedName("UNDER_BUDGET") UNDER_BUDGET,
    @SerializedName("EXACT") EXACT,
    @SerializedName("OVER_BUDGET") OVER_BUDGET
}

/**
 * BudgetListResponse — Wrapper cho danh sách budget trả về từ API.
 */
data class BudgetListResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<BudgetModel>?
)

/**
 * BudgetCreateRequest — Request body khi tạo kế hoạch ngân sách mới.
 */
data class BudgetCreateRequest(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("amount_limit") val amountLimit: Double,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("is_alert_enabled") val isAlertEnabled: Boolean = true,
    @SerializedName("alert_threshold") val alertThreshold: Double = 0.8
)

/**
 * BudgetUpdateRequest — Request body khi cập nhật kế hoạch ngân sách.
 */
data class BudgetUpdateRequest(
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("amount_limit") val amountLimit: Double? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("is_alert_enabled") val isAlertEnabled: Boolean? = null,
    @SerializedName("alert_threshold") val alertThreshold: Double? = null
)

/**
 * BudgetDetailResponse — Wrapper cho chi tiết budget trả về từ API.
 */
data class BudgetDetailResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BudgetModel?
)
