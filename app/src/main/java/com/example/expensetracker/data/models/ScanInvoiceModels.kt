package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

/** Wrapper response từ API /scan-invoice */
data class ScanInvoiceApiResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ScanInvoiceData?
)

/** Dữ liệu OCR trích xuất từ hóa đơn */
data class ScanInvoiceData(
    @SerializedName("amount") val amount: Double,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("note") val note: String?
)
