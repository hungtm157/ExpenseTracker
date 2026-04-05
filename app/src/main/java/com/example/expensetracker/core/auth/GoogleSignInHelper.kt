package com.example.expensetracker.core.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.expensetracker.App
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.GoogleLoginRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

/**
 * GoogleSignInHelper — Quản lý luồng Google Sign-In qua Firebase Auth.
 *
 * Luồng:
 * 1. Activity gọi `launchSignIn()` → mở Google Sign-In UI
 * 2. Activity nhận kết quả trong `onActivityResult` → gọi `handleResult(data)`
 * 3. Helper dùng Google ID Token xác thực với Firebase Auth
 * 4. Lấy thông tin user từ Firebase → gọi API backend `/api/v1/auth/google`
 * 5. Lưu token + thông tin đăng nhập → thông báo callback
 */
class GoogleSignInHelper(private val activity: Activity, private val listener: GoogleSignInListener) {

    companion object {
        const val RC_GOOGLE_SIGN_IN = 9001
        private const val TAG = "GoogleSignInHelper"
        // Web client ID từ google-services.json (client_type: 3)
        private const val WEB_CLIENT_ID = "1075433292104-3sdhhb368s8i03ktao7kkkv7ak4j4mgr.apps.googleusercontent.com"
    }

    interface GoogleSignInListener {
        fun onGoogleSignInSuccess()
        fun onGoogleSignInFailure(message: String)
        fun onGoogleSignInLoading(isLoading: Boolean)
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val apiService = ApiClient.create(ApiService::class.java)
    private val prefs = App.instance.preferences
    private val scope = CoroutineScope(Dispatchers.Main)

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    /** Bước 1: Mở giao diện chọn tài khoản Google */
    fun launchSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    /** Bước 2: Xử lý kết quả quay về từ Google Sign-In */
    fun handleResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                listener.onGoogleSignInLoading(true)
                firebaseAuthWithGoogle(idToken)
            } else {
                listener.onGoogleSignInFailure("Không lấy được thông tin xác thực từ Google")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In thất bại: ${e.statusCode}", e)
            listener.onGoogleSignInFailure("Đăng nhập Google thất bại (code: ${e.statusCode})")
        }
    }

    /** Bước 3: Dùng Google ID Token xác thực với Firebase Auth */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val email = firebaseUser.email ?: ""
                    val syncId = firebaseUser.uid
                    val fullName = firebaseUser.displayName
                    val avatar = firebaseUser.photoUrl?.toString()

                    Log.d(TAG, "Firebase Auth thành công — email: $email | uid: $syncId")
                    sendToBackend(email, syncId, fullName, avatar)
                } else {
                    listener.onGoogleSignInLoading(false)
                    listener.onGoogleSignInFailure("Không lấy được thông tin từ Firebase")
                }
            }
            .addOnFailureListener { e ->
                listener.onGoogleSignInLoading(false)
                Log.e(TAG, "Firebase Auth thất bại", e)
                listener.onGoogleSignInFailure("Xác thực Firebase thất bại")
            }
    }

    /** Bước 4: Gửi thông tin lên backend API */
    private fun sendToBackend(email: String, syncId: String, fullName: String?, avatar: String?) {
        scope.launch {
            try {
                val request = GoogleLoginRequest(email, syncId, fullName, avatar)
                val response = withContext(Dispatchers.IO) {
                    apiService.loginWithGoogle(request)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.data?.accessToken
                    val user = body?.data?.user

                    if (token != null && user != null) {
                        // Lưu thông tin đăng nhập
                        prefs.authToken = token
                        prefs.isLoggedIn = true
                        prefs.userName = user.fullName
                        prefs.userId = user.id.toLong()
                        prefs.userType = user.type

                        // Đồng bộ FCM token lên server
                        App.instance.syncFcmToken()

                        listener.onGoogleSignInLoading(false)
                        listener.onGoogleSignInSuccess()
                    } else {
                        listener.onGoogleSignInLoading(false)
                        listener.onGoogleSignInFailure("Phản hồi từ máy chủ không hợp lệ")
                    }
                } else {
                    listener.onGoogleSignInLoading(false)
                    val code = response.code()
                    when (code) {
                        403 -> listener.onGoogleSignInFailure("Tài khoản đã bị cấm")
                        else -> listener.onGoogleSignInFailure("Lỗi máy chủ ($code)")
                    }
                }
            } catch (e: Exception) {
                listener.onGoogleSignInLoading(false)
                Log.e(TAG, "Lỗi gọi API backend", e)
                listener.onGoogleSignInFailure("Không thể kết nối đến máy chủ")
            }
        }
    }

}
