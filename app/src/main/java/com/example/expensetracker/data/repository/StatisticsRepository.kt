package com.example.expensetracker.data.repository

import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.GeneralStatisticsResponse
import retrofit2.Response

class StatisticsRepository(private val apiService: ApiService) {
    suspend fun getStatisticsGeneral(): Response<GeneralStatisticsResponse> {
        return apiService.getStatisticsGeneral()
    }
}
