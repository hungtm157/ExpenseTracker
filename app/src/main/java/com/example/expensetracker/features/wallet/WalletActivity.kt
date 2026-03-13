package com.example.expensetracker.features.wallet

import android.app.AlertDialog
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.App
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.WalletRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DecimalFormat

/**
 * WalletActivity — Màn hình Quản lý ví.
 */
class WalletActivity : BaseActivity(R.layout.activity_wallet), WalletListener {

    private lateinit var btnBack: ImageView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvActiveWalletsCount: TextView
    private lateinit var recyclerWallets: RecyclerView
    private lateinit var fabAddWallet: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: WalletAdapter
    private lateinit var controller: WalletController
    
    private val decimalFormat = DecimalFormat("#,###")

    override fun initViews() {
        btnBack = findViewById(R.id.imgBack)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvActiveWalletsCount = findViewById(R.id.tvActiveWalletsCount)
        recyclerWallets = findViewById(R.id.recyclerWallets)
        fabAddWallet = findViewById(R.id.fabAddWallet)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Init Repository & Controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = WalletRepository(apiService)
        controller = WalletController(repository, this)

        // Init Adapter
        adapter = WalletAdapter(
            emptyList(),
            onStatusChanged = { wallet, isChecked ->
                val newStatus = if (isChecked) WalletStatus.ACTIVATE else WalletStatus.DISABLED
                controller.updateWallet(wallet.id, WalletUpdateRequest(status = newStatus))
            },
            onEditClick = { showAddEditDialog(it) },
            onDeleteClick = { showDeleteConfirmation(it) }
        )

        recyclerWallets.layoutManager = LinearLayoutManager(this)
        recyclerWallets.adapter = adapter

        controller.loadWallets()
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }
        fabAddWallet.setOnClickListener { showAddEditDialog() }
    }

    private fun showAddEditDialog(wallet: WalletModel? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_wallet, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = dialogView.findViewById<EditText>(R.id.etWalletName)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgWalletType)
        val etBalance = dialogView.findViewById<EditText>(R.id.etInitialBalance)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnClose)

        if (wallet != null) {
            tvDialogTitle.text = "Sửa ví"
            etName.setText(wallet.name)
            etBalance.setText(wallet.balance.toString())
            when (wallet.type) {
                WalletType.CASH -> rgType.check(R.id.rbCash)
                WalletType.BANK_ACCOUNT -> rgType.check(R.id.rbBank)
                WalletType.E_WALLET -> rgType.check(R.id.rbEWallet)
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val balance = etBalance.text.toString().toDoubleOrNull() ?: 0.0
            val type = when (rgType.checkedRadioButtonId) {
                R.id.rbCash -> WalletType.CASH
                R.id.rbBank -> WalletType.BANK_ACCOUNT
                R.id.rbEWallet -> WalletType.E_WALLET
                else -> WalletType.CASH
            }

            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (wallet == null) {
                controller.createWallet(name, type, balance, "VND")
            } else {
                controller.updateWallet(wallet.id, WalletUpdateRequest(name, type, balance))
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showDeleteConfirmation(wallet: WalletModel) {
        AlertDialog.Builder(this)
            .setTitle("Xóa ví")
            .setMessage("Bạn có chắc chắn muốn xóa ví '${wallet.name}'?")
            .setPositiveButton("Xóa") { _, _ -> controller.deleteWallet(wallet.id) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // ─── WalletListener Callbacks ───────────────────────────────────────────

    override fun onWalletsLoaded(wallets: List<WalletModel>) {
        adapter.updateData(wallets)
        tvEmpty.visibility = if (wallets.isEmpty()) View.VISIBLE else View.GONE
        
        // Update Summary
        val total = wallets.sumOf { it.balance }
        val activeCount = wallets.count { it.status == WalletStatus.ACTIVATE }
        
        tvTotalBalance.text = "${decimalFormat.format(total)} đ"
        tvActiveWalletsCount.text = "$activeCount ví đang hoạt động"
    }

    override fun onWalletCreated(wallet: WalletModel) {
        Toast.makeText(this, "Đã thêm ví: ${wallet.name}", Toast.LENGTH_SHORT).show()
        controller.loadWallets()
    }

    override fun onWalletUpdated(wallet: WalletModel) {
        Toast.makeText(this, "Đã cập nhật ví: ${wallet.name}", Toast.LENGTH_SHORT).show()
        controller.loadWallets()
    }

    override fun onWalletDeleted(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        controller.loadWallets()
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
