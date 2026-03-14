package com.example.expensetracker.features.transaction

/**
 * Listener cho màn hình Lịch sử Giao dịch.
 */
interface TransactionHistoryListener {
    fun onLoading(isLoading: Boolean)
    fun onTransactionsLoaded(transactions: List<TransactionModel>)
    fun onError(message: String)
    fun onDeleteSuccess(transactionId: Int)
}
