package com.example.expensetracker.features.wallet

import com.google.gson.annotations.SerializedName

/**
 * WalletModel — Model đại diện cho một ví tiền.
 */
data class WalletModel(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("balance") val balance: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("type") val type: WalletType,
    @SerializedName("status") val status: WalletStatus,
    @SerializedName("createdAt") val createdAt: String? = null
)

enum class WalletType {
    @SerializedName("CASH") CASH,
    @SerializedName("BANK_ACCOUNT") BANK_ACCOUNT,
    @SerializedName("E_WALLET") E_WALLET
}

enum class WalletStatus {
    @SerializedName("ACTIVATE") ACTIVATE,
    @SerializedName("DISABLED") DISABLED
}

data class WalletListResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: WalletListData?
)

data class WalletListData(
    @SerializedName("items") val items: List<WalletModel>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class WalletCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: WalletType,
    @SerializedName("balance") val balance: Double? = 0.0,
    @SerializedName("currency") val currency: String? = "VND"
)

data class WalletUpdateRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: WalletType? = null,
    @SerializedName("balance") val balance: Double? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("status") val status: WalletStatus? = null
)

data class WalletDeleteResponse(
    @SerializedName("message") val message: String
)
