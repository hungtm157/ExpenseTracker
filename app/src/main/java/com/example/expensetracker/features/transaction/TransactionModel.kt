package com.example.expensetracker.features.transaction

import com.google.gson.annotations.SerializedName

/**
 * TransactionModel — Model đại diện cho một giao dịch.
 */
data class TransactionModel(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("wallet_id") val walletId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("note") val note: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("source") val source: TransactionSource,
    @SerializedName("receipt_image_url") val receiptImageUrl: String?,
    @SerializedName("status") val status: TransactionStatus,
    @SerializedName("transaction_date") val transactionDate: String, // String for ISO format from backend
    @SerializedName("createdAt") val createdAt: String? = null
)

enum class TransactionSource {
    @SerializedName("MANUAL") MANUAL,
    @SerializedName("OCR_SCAN") OCR_SCAN
}

enum class TransactionStatus {
    @SerializedName("ACTIVATE") ACTIVATE,
    @SerializedName("DISABLED") DISABLED
}

/**
 * TransactionListResponse — Wrapper cho danh sách giao dịch trả về từ API.
 */
data class TransactionListResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TransactionListData?
)

data class TransactionListData(
    @SerializedName("items") val items: List<TransactionModel>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

/**
 * TransactionCreateRequest — Request body khi tạo giao dịch mới.
 */
data class TransactionCreateRequest(
    @SerializedName("wallet_id") val walletId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("note") val note: String? = null,
    @SerializedName("currency") val currency: String? = "VND",
    @SerializedName("source") val source: TransactionSource? = TransactionSource.MANUAL,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("receipt_image_url") val receiptImageUrl: String? = null
)

/**
 * TransactionUpdateRequest — Request body khi cập nhật giao dịch.
 */
data class TransactionUpdateRequest(
    @SerializedName("wallet_id") val walletId: Int? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("source") val source: TransactionSource? = null,
    @SerializedName("transaction_date") val transactionDate: String? = null,
    @SerializedName("receipt_image_url") val receiptImageUrl: String? = null,
    @SerializedName("status") val status: TransactionStatus? = null
)
