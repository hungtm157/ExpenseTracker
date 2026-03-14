package com.example.expensetracker.features.transaction

/**
 * TransactionListener — Interface định nghĩa các callback cho màn hình Giao dịch.
 */
interface TransactionListener {
    fun onTransactionCreated(transaction: TransactionModel)
    fun onTransactionUpdated(transaction: TransactionModel)
    fun onTransactionDeleted()
    fun onOcrResult(transaction: TransactionModel)
    fun onLoading(isLoading: Boolean)
    fun onError(message: String)
}
