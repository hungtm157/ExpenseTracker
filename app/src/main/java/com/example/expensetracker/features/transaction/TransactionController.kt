package com.example.expensetracker.features.transaction

import android.util.Log
import com.example.expensetracker.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * TransactionController — Xử lý logic nghiệp vụ cho màn hình Giao dịch.
 */
class TransactionController(
    private val repository: TransactionRepository,
    private val listener: TransactionListener
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "TransactionController"

    /** Tạo giao dịch mới (Thủ công) */
    fun createTransaction(
        walletId: Int,
        categoryId: Int,
        amount: Double,
        date: String,
        note: String? = null,
        imageFile: File? = null
    ) {
        Log.d(TAG, "createTransaction: Đang tạo giao dịch...")
        listener.onLoading(true)

        val walletIdBody = walletId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val amountBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dateBody = date.toRequestBody("text/plain".toMediaTypeOrNull())
        val noteBody = note?.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("receipt_image", it.name, requestFile)
        }

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createTransaction(
                        walletIdBody, categoryIdBody, amountBody, dateBody, noteBody, null, imagePart
                    )
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "createTransaction: Thành công ID ${it.id}")
                        listener.onTransactionCreated(it)
                    }
                } else {
                    val errorMsg = "Lỗi: ${response.code()}"
                    Log.e(TAG, "createTransaction: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createTransaction: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi kết nối")
            }
        }
    }

    /** Quét OCR */
    fun scanOcr(walletId: Int, categoryId: Int, imageFile: File) {
        Log.d(TAG, "scanOcr: Bắt đầu quét OCR...")
        listener.onLoading(true)

        val walletIdBody = walletId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            "receipt_image", imageFile.name, imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.ocrScan(walletIdBody, categoryIdBody, imagePart)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "scanOcr: Thành công, nhận diện giao dịch")
                        listener.onOcrResult(it)
                    }
                } else {
                    val errorMsg = "Lỗi OCR: ${response.code()}"
                    Log.e(TAG, "scanOcr: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "scanOcr: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError(e.message ?: "Lỗi kết nối")
            }
        }
    }
}
