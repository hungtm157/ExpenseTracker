package com.example.expensetracker.data.repository

import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.features.plan.*
import retrofit2.Response

/**
 * BudgetRepository — Lớp trung gian xử lý dữ liệu Kế hoạch ngân sách từ API.
 */
class BudgetRepository(private val apiService: ApiService) {

    /** Lấy danh sách kế hoạch ngân sách (có hỗ trợ lọc theo ngày) */
    suspend fun getBudgets(fromDate: String? = null, toDate: String? = null): Response<BudgetListResponse> {
        return apiService.getBudgets(fromDate, toDate)
    }

    /** Lấy chi tiết kế hoạch ngân sách */
    suspend fun getBudgetDetail(id: Int): Response<BudgetDetailResponse> {
        return apiService.getBudgetDetail(id)
    }

    /** Tạo kế hoạch ngân sách mới */
    suspend fun createBudget(request: BudgetCreateRequest): Response<BudgetModel> {
        return apiService.createBudget(request)
    }

    /** Hoàn thành và chốt kế hoạch ngân sách */
    suspend fun completeBudget(id: Int): Response<BudgetModel> {
        return apiService.completeBudget(id)
    }

    /** Cập nhật kế hoạch ngân sách */
    suspend fun updateBudget(id: Int, request: BudgetUpdateRequest): Response<BudgetModel> {
        return apiService.updateBudget(id, request)
    }
}
