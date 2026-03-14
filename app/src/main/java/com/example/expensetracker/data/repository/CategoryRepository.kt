package com.example.expensetracker.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.models.CategoryListResponse
import com.example.expensetracker.data.models.ErrorResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

/**
 * CategoryRepository — Lớp Model quản lý dữ liệu danh mục.
 * Đóng gói logic gọi API và xử lý dữ liệu (Multipart).
 */
class CategoryRepository {

    private val api = ApiClient.create(ApiService::class.java)

    /**
     * Lấy danh sách danh mục từ API.
     */
    suspend fun getCategories(type: String): Response<CategoryListResponse> {
        return api.getCategories(
            page = 1,
            type = type
        )
    }

    /**
     * Tạo danh mục mới (Multipart).
     */
    suspend fun createCategory(
        context: Context,
        token: String,
        name: String,
        type: String,
        imageUri: Uri?
    ): Response<CategoryItem> {
        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = prepareImagePart(context, imageUri)

        return api.createCategory(
            name = nameBody,
            type = typeBody,
            icon = imagePart
        )
    }

    /**
     * Cập nhật danh mục hiện có (Multipart PATCH).
     */
    suspend fun updateCategory(
        context: Context,
        token: String,
        id: Int,
        name: String?,
        type: String?,
        status: String?,
        imageUri: Uri?
    ): Response<CategoryItem> {
        val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type?.toRequestBody("text/plain".toMediaTypeOrNull())
        val statusBody = status?.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = prepareImagePart(context, imageUri)

        return api.updateCategory(
            id = id,
            name = nameBody,
            type = typeBody,
            status = statusBody,
            icon = imagePart
        )
    }

    /**
     * Xóa danh mục (DELETE).
     */
    suspend fun deleteCategory(token: String, id: Int): Response<ErrorResponse> {
        return api.deleteCategory(
            id = id
        )
    }

    /**
     * Helper chuyển Uri sang MultipartBody.Part
     */
    private fun prepareImagePart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null
        
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            bytes?.let {
                val requestFile = it.toRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("icon", "category_icon.$extension", requestFile)
            }
        } catch (e: Exception) {
            null
        }
    }
}
