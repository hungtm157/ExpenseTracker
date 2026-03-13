package com.example.expensetracker.features.category

import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.CategoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CategoryController — Xử lý logic gọi API danh mục.
 * Theo pattern MVC của dự án: Controller gọi API → callback qua Listener.
 */
class CategoryController(private val listener: CategoryListener) {

    private val api = ApiClient.create(ApiService::class.java)
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Tải danh sách danh mục theo loại (EXPENSE hoặc INCOME).
     * @param token      Bearer token lấy từ AppPreferences.authToken
     * @param type       "EXPENSE" hoặc "INCOME"
     */
    fun loadCategories(token: String, type: String) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getCategories(
                        token = "Bearer $token",
                        page = 1,
                        limit = 100,
                        type = type
                    )
                }
                listener.onLoading(false)

                if (response.isSuccessful) {
                    val items = response.body()?.data?.items ?: emptyList()
                    listener.onCategoriesLoaded(items)
                } else {
                    listener.onError("Lỗi ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Không thể tải danh mục")
            }
        }
    }

    interface CategoryListener {
        fun onCategoriesLoaded(items: List<CategoryItem>)
        fun onLoading(isLoading: Boolean)
        fun onError(message: String)
    }
}
