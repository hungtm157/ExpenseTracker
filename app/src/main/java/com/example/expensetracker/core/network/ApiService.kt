package com.example.expensetracker.core.network

import retrofit2.http.GET

/**
 * ApiService — Interface định nghĩa các API endpoint.
 * Thêm các endpoint thực tế khi tích hợp backend.
 *
 * Ví dụ:
 * @GET("expenses")
 * suspend fun getExpenses(): List<ExpenseResponse>
 */
interface ApiService {

    // TODO: Định nghĩa các endpoint tại đây
    @GET("ping")
    suspend fun ping(): Any
}
