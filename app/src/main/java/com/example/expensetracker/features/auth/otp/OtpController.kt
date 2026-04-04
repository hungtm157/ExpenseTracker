package com.example.expensetracker.features.auth.otp

import com.example.expensetracker.App
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.VerifyOtpRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpController(private val listener: OtpListener) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val apiService = ApiClient.create(ApiService::class.java)
    private val prefs = App.instance.preferences

    fun verifyOtp(fullName: String, email: String, password: String, otp: String) {
        if (otp.length != 6) {
            listener.onVerifyFailure("Vui lòng nhập đủ 6 chữ số OTP")
            return
        }

        listener.onVerifyLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.verifyOtp(VerifyOtpRequest(fullName, email, password, otp))
                }

                listener.onVerifyLoading(false)

                val body = response.body()
                val httpStatus = response.code()
                val bodyStatus = body?.status

                when {
                    (httpStatus == 200 || httpStatus == 201) && (bodyStatus == 200 || bodyStatus == 201) -> {
                        val token = body?.data?.accessToken
                        val user = body?.data?.user

                        if (token != null && user != null) {
                            prefs.authToken = token
                            prefs.isLoggedIn = true
                            prefs.userName = user.fullName
                            prefs.userId = user.id.toLong()
                            prefs.userType = user.type
                            listener.onVerifySuccess()
                        } else {
                            listener.onVerifyFailure("Phản hồi từ máy chủ không hợp lệ")
                        }
                    }
                    else -> {
                        val errorMsg = parseErrorMessage(response.errorBody())
                        listener.onVerifyFailure(errorMsg)
                    }
                }
            } catch (e: Exception) {
                listener.onVerifyLoading(false)
                listener.onVerifyFailure("Không thể kết nối đến máy chủ")
            }
        }
    }

    fun resendOtp(email: String) {
        if (email.isEmpty()) {
            listener.onResendOtpFailure("Email không hợp lệ")
            return
        }

        listener.onVerifyLoading(true)

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.resendOtp(com.example.expensetracker.data.models.ResendOtpRequest(email))
                }

                listener.onVerifyLoading(false)

                val body = response.body()
                val httpStatus = response.code()
                val bodyStatus = body?.status

                if ((httpStatus == 200 || httpStatus == 201) && (bodyStatus == 200 || bodyStatus == 201)) {
                    listener.onResendOtpSuccess()
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    listener.onResendOtpFailure(errorMsg)
                }
            } catch (e: Exception) {
                listener.onVerifyLoading(false)
                listener.onResendOtpFailure("Không thể kết nối đến máy chủ")
            }
        }
    }

    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?): String {
        val errorJson = errorBody?.string() ?: return "Đã có lỗi xảy ra, vui lòng thử lại"
        return try {
            val json = Gson().fromJson(errorJson, JsonObject::class.java)
            
            // Ưu tiên data.error (chi tiết hơn)
            if (json.has("data") && json.get("data").isJsonObject) {
                val data = json.getAsJsonObject("data")
                if (data.has("error") && !data.get("error").isJsonNull) {
                    return data.get("error").asString
                }
            }
            
            // Sau đó là message chung
            if (json.has("message") && !json.get("message").isJsonNull) {
                return json.get("message").asString
            }
            
            "Đã có lỗi xảy ra"
        } catch (e: Exception) {
            if (errorJson.length < 100) errorJson else "Đã có lỗi xảy ra"
        }
    }
}
