package com.example.expensetracker.features.category

import android.content.Context
import android.net.Uri
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.models.ErrorResponse
import com.example.expensetracker.data.repository.CategoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

/**
 * CategoryController — Lớp Controller điều phối luồng dữ liệu.
 * Theo mô hình MVC: Nhận yêu cầu từ View, gọi Repository xử lý và trả về View qua Listener.
 */
class CategoryController(private val listener: CategoryListener) {

    private val repository = CategoryRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Yêu cầu Model tải danh sách danh mục.
     */
    fun loadCategories(token: String, type: String) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getCategories(token, type)
                }
                listener.onLoading(false)

                if (response.isSuccessful) {
                    val items = response.body()?.data?.items ?: emptyList()
                    listener.onCategoriesLoaded(items)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Không thể tải danh mục")
            }
        }
    }

    /**
     * Yêu cầu Model thêm danh mục mới.
     */
    fun addCategory(context: Context, token: String, name: String, type: String, imageUri: Uri?) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createCategory(context, token, name, type, imageUri)
                }
                
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { listener.onCategoryAdded(it) }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi thêm danh mục")
            }
        }
    }

    /**
     * Yêu cầu Model cập nhật danh mục.
     */
    fun updateCategory(
        context: Context,
        token: String,
        id: Int,
        name: String?,
        type: String?,
        status: String?,
        imageUri: Uri?
    ) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateCategory(context, token, id, name, type, status, imageUri)
                }

                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let { listener.onCategoryUpdated(it) }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi cập nhật danh mục")
            }
        }
    }

    /**
     * Yêu cầu Model xóa danh mục.
     */
    fun deleteCategory(token: String, id: Int) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.deleteCategory(token, id)
                }

                listener.onLoading(false)
                if (response.isSuccessful) {
                    listener.onCategoryDeleted(response.body()?.message ?: "Xóa thành công")
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi khi xóa danh mục")
            }
        }
    }

    /**
     * Helper trích xuất thông báo lỗi từ JSON body của API.
     */
    private fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val errorJson = errorBody?.string()
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "Đã có lỗi xảy ra, vui lòng thử lại"
        }
    }

    interface CategoryListener {
        fun onCategoriesLoaded(items: List<CategoryItem>)
        fun onCategoryAdded(item: CategoryItem)
        fun onCategoryUpdated(item: CategoryItem)
        fun onCategoryDeleted(message: String)
        fun onLoading(isLoading: Boolean)
        fun onError(message: String)
    }
}
