package com.example.expensetracker.data.models

import com.google.gson.annotations.SerializedName

// 1. General Statistics
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

// 2. Statistics By Category
data class StatisticsByCategoryResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<StatisticsByCategoryData>?
)

data class StatisticsByCategoryData(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("category_icon") val categoryIcon: String?,
    @SerializedName("total_amount") val totalAmount: Long
)

// 3. Statistics Trend
data class StatisticsTrendResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<StatisticsTrendData>?
)

data class StatisticsTrendData(
    @SerializedName("date") val date: String,
    @SerializedName("income") val income: Long,
    @SerializedName("expense") val expense: Long
)

// 4. Statistics Expense to Balance Ratio
data class StatisticsExpenseToBalanceRatioResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StatisticsExpenseToBalanceData?
)

data class StatisticsExpenseToBalanceData(
    @SerializedName("total_balance") val totalBalance: Long,
    @SerializedName("total_expense") val totalExpense: Long
)

// 5. Statistics Income vs Expense
data class StatisticsIncomeVsExpenseResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: StatisticsIncomeVsExpenseData?
)

data class StatisticsIncomeVsExpenseData(
    @SerializedName("INCOME") val income: IncomeExpenseDetail?,
    @SerializedName("EXPENSE") val expense: IncomeExpenseDetail?
)

data class IncomeExpenseDetail(
    @SerializedName("total_amount") val totalAmount: Long,
    @SerializedName("transaction_count") val transactionCount: Int
)
