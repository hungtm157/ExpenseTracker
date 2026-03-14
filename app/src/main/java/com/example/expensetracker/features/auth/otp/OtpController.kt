package com.example.expensetracker.features.auth.otp

import com.example.expensetracker.App
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.VerifyOtpRequest
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
                            listener.onVerifySuccess()
                        } else {
                            listener.onVerifyFailure("Phản hồi từ máy chủ không hợp lệ")
                        }
                    }
                    httpStatus == 400 || bodyStatus == 400 -> listener.onVerifyFailure("Mã OTP không đúng hoặc đã hết hạn")
                    httpStatus == 404 || bodyStatus == 404 -> listener.onVerifyFailure("Email chưa được đăng ký")
                    else -> listener.onVerifyFailure("Xác thực thất bại ($httpStatus)")
                }
            } catch (e: Exception) {
                listener.onVerifyLoading(false)
                listener.onVerifyFailure("Không thể kết nối đến máy chủ")
            }
        }
    }
}
