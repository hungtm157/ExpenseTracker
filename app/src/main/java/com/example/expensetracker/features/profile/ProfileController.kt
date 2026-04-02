package com.example.expensetracker.features.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.expensetracker.App
import com.example.expensetracker.data.models.ErrorResponse
import com.example.expensetracker.data.models.UpdateNameRequest
import com.example.expensetracker.data.models.UserProfileResponse
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException

interface ProfileListener {
    fun onLoading(isLoading: Boolean)
    fun onProfileLoaded(profile: UserProfileResponse)
    fun onError(message: String)
    fun onNameUpdated(profile: UserProfileResponse)
    fun onNameUpdateError(message: String)
    fun onAvatarUpdated(profile: UserProfileResponse)
    fun onAvatarUpdateError(message: String)
    fun onStatsLoaded(transactionCount: Int, budgetCount: Int)
}

class ProfileController(
    private val repository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val listener: ProfileListener
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
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
                    val profile = response.body()?.data
                    if (profile != null) {
                        // Sync with local favorites
                        App.instance.preferences.userType = profile.type
                        App.instance.preferences.userName = profile.fullName
                        
                        listener.onProfileLoaded(profile)
                    } else {
                        listener.onError("Không lấy được thông tin người dùng")
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
                val request = UpdateNameRequest(fullName)
                val response = withContext(Dispatchers.IO) {
                    repository.updateName(request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val profile = response.body()?.data
                    if (profile != null) {
                        // Sync with local favorites
                        App.instance.preferences.userName = profile.fullName
                        
                        listener.onNameUpdated(profile)
                    } else {
                        listener.onNameUpdateError("Không nhận được phản hồi từ server")
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
                    val profile = response.body()?.data
                    if (profile != null) {
                        // Sync with local favorites (optional, in case avatar URL changed or something)
                        // but usually it doesn't affect userType. 
                        // But let's sync everything for consistency if needed.
                        
                        listener.onAvatarUpdated(profile)
                    } else {
                        listener.onAvatarUpdateError("Không nhận được phản hồi từ server")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onAvatarUpdateError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi updateAvatar: ", e)
                listener.onLoading(false)
                listener.onAvatarUpdateError("Không thể kết nối đến máy chủ")
            }
        }
    }

    fun fetchProfileStats() {
        scope.launch {
            try {
                // Sử dụng supervisorScope để các yêu cầu độc lập với nhau
                supervisorScope {
                    val transactionsDef = async(Dispatchers.IO) {
                        transactionRepository.getTransactions(
                            token = App.instance.preferences.authToken,
                            limit = 1
                        )
                    }
                    val budgetsDef = async(Dispatchers.IO) {
                        budgetRepository.getBudgets()
                    }

                    try {
                        val transactionsResponse = transactionsDef.await()
                        val budgetsResponse = budgetsDef.await()

                        var txCount = 0
                        var budgetCount = 0

                        if (transactionsResponse.isSuccessful) {
                            txCount = transactionsResponse.body()?.data?.total ?: 0
                        }
                        
                        if (budgetsResponse.isSuccessful) {
                            budgetCount = budgetsResponse.body()?.data?.size ?: 0
                        }

                        listener.onStatsLoaded(txCount, budgetCount)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.e(TAG, "Lỗi khi đợi kết quả fetchProfileStats: ", e)
                        listener.onStatsLoaded(0, 0)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                Log.e(TAG, "Lỗi fetchProfileStats: ", e)
                listener.onStatsLoaded(0, 0)
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
