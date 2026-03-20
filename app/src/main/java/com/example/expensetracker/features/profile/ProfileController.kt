package com.example.expensetracker.features.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.expensetracker.data.models.ErrorResponse
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

interface ProfileListener {
    fun onLoading(isLoading: Boolean)
    fun onProfileLoaded(profile: UserProfileResponse)
    fun onError(message: String)
    fun onNameUpdated(profile: UserProfileResponse)
    fun onNameUpdateError(message: String)
    fun onAvatarUpdated(profile: UserProfileResponse)
    fun onAvatarUpdateError(message: String)
}

class ProfileController(
    private val repository: AuthRepository,
    private val listener: ProfileListener
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "ProfileController"

    fun fetchProfile() {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getProfile()
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        listener.onProfileLoaded(it)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi fetch Profile: ", e)
                listener.onLoading(false)
                listener.onError("Lỗi kết nối. Vui lòng thử lại sau.")
            }
        }
    }

    fun updateName(fullName: String) {
        listener.onLoading(true)
        scope.launch {
            try {
                val request = com.example.expensetracker.data.models.UpdateNameRequest(fullName)
                val response = withContext(Dispatchers.IO) {
                    repository.updateName(request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        listener.onNameUpdated(it)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onNameUpdateError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi updateName: ", e)
                listener.onLoading(false)
                listener.onNameUpdateError("Lỗi kết nối. Vui lòng thử lại sau.")
            }
        }
    }

    fun updateAvatar(context: Context, imageUri: Uri) {
        listener.onLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateAvatar(context, imageUri)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        listener.onAvatarUpdated(it)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onAvatarUpdateError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi updateAvatar: ", e)
                listener.onLoading(false)
                listener.onAvatarUpdateError(e.message ?: "Lỗi kết nối. Vui lòng thử lại sau.")
            }
        }
    }

    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?): String {
        val errorJson = errorBody?.string() ?: return "Đã có lỗi xảy ra"
        return try {
            // Thử parse theo ErrorResponse JSON
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            // Nếu không phải JSON (ví dụ plain text STRING), trả về chính nội dung đó nếu không quá dài
            if (errorJson.length < 100) errorJson else "Đã có lỗi xảy ra"
        }
    }
}
