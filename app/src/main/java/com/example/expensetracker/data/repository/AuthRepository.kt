package com.example.expensetracker.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.ProfileApiResponse
import com.example.expensetracker.data.models.UpdateNameRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    suspend fun getProfile(): Response<ProfileApiResponse> {
        return apiService.getProfile()
    }

    suspend fun updateName(request: UpdateNameRequest): Response<ProfileApiResponse> {
        return apiService.updateName(request)
    }

    suspend fun updateAvatar(context: Context, imageUri: Uri): Response<ProfileApiResponse> {
        val avatarPart = prepareAvatarPart(context, imageUri)
        return apiService.updateAvatar(avatarPart ?: throw Exception("Không thể xử lý file ảnh"))
    }

    private fun prepareAvatarPart(context: Context, uri: Uri?): MultipartBody.Part? {
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
                MultipartBody.Part.createFormData("avatar", "avatar.$extension", requestFile)
            }
        } catch (e: Exception) {
            null
        }
    }
}
