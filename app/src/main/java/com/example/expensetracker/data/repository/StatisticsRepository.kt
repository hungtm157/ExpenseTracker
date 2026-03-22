package com.example.expensetracker.data.repository

import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.*
import retrofit2.Response

class StatisticsRepository(private val apiService: ApiService) {
    suspend fun getStatisticsGeneral(
        fromDate: String? = null,
        toDate: String? = null
    ): Response<GeneralStatisticsResponse> {
        return apiService.getStatisticsGeneral(fromDate, toDate)
    }

    suspend fun getStatisticsByCategory(
        type: String,
        fromDate: String? = null,
        toDate: String? = null
    ): Response<StatisticsByCategoryResponse> {
        return apiService.getStatisticsByCategory(type, fromDate, toDate)
    }

    suspend fun getStatisticsTrend(
        period: String,
        fromDate: String? = null,
        toDate: String? = null
    ): Response<StatisticsTrendResponse> {
        return apiService.getStatisticsTrend(period, fromDate, toDate)
    }

    suspend fun getStatisticsExpenseToBalanceRatio(
        fromDate: String? = null,
        toDate: String? = null
    ): Response<StatisticsExpenseToBalanceRatioResponse> {
        return apiService.getStatisticsExpenseToBalanceRatio(fromDate, toDate)
    }

    suspend fun getStatisticsIncomeVsExpense(
        fromDate: String? = null,
        toDate: String? = null
    ): Response<StatisticsIncomeVsExpenseResponse> {
        return apiService.getStatisticsIncomeVsExpense(fromDate, toDate)
    }
}
