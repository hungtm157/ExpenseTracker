package com.example.expensetracker.features.transaction

import android.util.Log
import com.example.expensetracker.data.repository.TransactionRepository
import com.google.gson.Gson
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

    /** Tạo giao dịch mới (Thủ công - JSON) */
    fun createTransaction(
        token: String,
        walletId: Int,
        categoryId: Int,
        amount: Double,
        date: String,
        note: String? = null,
        currency: String? = null,
        source: TransactionSource = TransactionSource.MANUAL,
        imageFile: File? = null
    ) {
        Log.d(TAG, "createTransaction: Đang tạo giao dịch (JSON)...")
        listener.onLoading(true)

        val request = TransactionCreateRequest(
            walletId = walletId,
            categoryId = categoryId,
            amount = amount,
            note = note,
            currency = currency ?: "VND",
            transactionDate = date,
            source = source
        )

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.createTransaction(request)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "createTransaction: Thành công ID ${it.id}")
                        listener.onTransactionCreated(it)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "createTransaction: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "createTransaction: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Cập nhật giao dịch */
    fun updateTransaction(
        id: Int,
        walletId: Int,
        categoryId: Int,
        amount: Double,
        date: String,
        note: String? = null,
        currency: String? = null
    ) {
        Log.d(TAG, "updateTransaction: Đang cập nhật giao dịch ID $id...")
        listener.onLoading(true)

        val walletIdBody = walletId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val amountBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dateBody = date.toRequestBody("text/plain".toMediaTypeOrNull())
        val noteBody = note?.toRequestBody("text/plain".toMediaTypeOrNull())
        val currencyBody = (currency ?: "VND").toRequestBody("text/plain".toMediaTypeOrNull())

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.updateTransaction(
                        id = id,
                        walletId = walletIdBody,
                        categoryId = categoryIdBody,
                        amount = amountBody,
                        transactionDate = dateBody,
                        note = noteBody,
                        currency = currencyBody
                    )
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d(TAG, "updateTransaction: Cập nhật thành công ID ${it.id}")
                        listener.onTransactionUpdated(it)
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "updateTransaction: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateTransaction: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /** Quét hóa đơn — API mới /scan-invoice, chỉ cần ảnh */
    fun scanInvoice(imageFile: File) {
        Log.d(TAG, "scanInvoice: Bắt đầu quét hóa đơn...")
        listener.onLoading(true)

        val imagePart = MultipartBody.Part.createFormData(
            "image", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.scanInvoice(imagePart)
                }
                listener.onLoading(false)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        Log.d(TAG, "scanInvoice: Thành công — amount=${data.amount}, category=${data.categoryId}")
                        listener.onScanInvoiceResult(data)
                    } else {
                        listener.onError("Không nhận được dữ liệu từ server")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    Log.e(TAG, "scanInvoice: $errorMsg")
                    listener.onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "scanInvoice: Lỗi kết nối", e)
                listener.onLoading(false)
                listener.onError("Không thể kết nối đến máy chủ")
            }
        }
    }

    /**
     * Helper trích xuất thông báo lỗi từ JSON body của API.
     */
    private fun parseErrorMessage(errorBody: okhttp3.ResponseBody?): String {
        return try {
            val errorJson = errorBody?.string()
            val errorResponse = Gson().fromJson(errorJson, com.example.expensetracker.data.models.ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "Đã có lỗi xảy ra, vui lòng thử lại"
        }
    }
}
