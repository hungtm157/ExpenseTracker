package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

data class GeneralStatisticsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: GeneralStatisticsData?
)

data class GeneralStatisticsData(
    @SerializedName("total_balance") val totalBalance: Long,
    @SerializedName("total_income") val totalIncome: Long,
    @SerializedName("total_expense") val totalExpense: Long
)
