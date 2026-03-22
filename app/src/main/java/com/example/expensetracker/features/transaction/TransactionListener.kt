package com.example.expensetracker.features.transaction

import com.example.expensetracker.data.models.ScanInvoiceData

/**
 * TransactionListener — Interface định nghĩa các callback cho màn hình Giao dịch.
 */
interface TransactionListener {
    fun onTransactionCreated(transaction: TransactionModel)
    fun onTransactionUpdated(transaction: TransactionModel)
    fun onTransactionDeleted()
    fun onScanInvoiceResult(data: ScanInvoiceData)
    fun onLoading(isLoading: Boolean)
    fun onError(message: String)
}

