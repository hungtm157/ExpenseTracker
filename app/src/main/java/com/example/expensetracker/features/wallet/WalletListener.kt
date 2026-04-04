package com.example.expensetracker.features.wallet

/**
 * WalletListener — Interface định nghĩa các callback cho màn hình Quản lý ví.
 */
interface WalletListener {
    fun onWalletsLoaded(wallets: List<WalletModel>)
    fun onWalletCreated(wallet: WalletModel)
    fun onWalletUpdated(wallet: WalletModel)
    fun onWalletDeleted(message: String)
    fun onTransactionCountReceived(count: Int, wallet: WalletModel)
    fun onLoading(isLoading: Boolean)
    fun onError(message: String)
}
