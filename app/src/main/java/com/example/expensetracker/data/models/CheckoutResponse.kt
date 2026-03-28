package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

/**
 * CheckoutResponse — Response từ API /api/payment/checkout
 */
data class CheckoutResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CheckoutData?
)

data class CheckoutData(
    @SerializedName("amount") val amount: Int,
    @SerializedName("transferContent") val transferContent: String,
    @SerializedName("qrUrl") val qrUrl: String
)
