package com.example.expensetracker.features.transaction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.utils.DateTimeUtils
import com.google.gson.Gson
import java.text.DecimalFormat
import androidx.activity.result.contract.ActivityResultContracts

class TransactionDetailActivity : BaseActivity(R.layout.activity_transaction_detail) {

    private lateinit var btnBack: ImageView
    private lateinit var btnEdit: ImageView
    private lateinit var btnDelete: ImageView
    private lateinit var layoutHeader: LinearLayout
    
    private lateinit var tvType: TextView
    private lateinit var tvAmount: TextView
    
    private lateinit var tvWalletName: TextView
    private lateinit var tvWalletType: TextView
    private lateinit var ivWalletIcon: ImageView
    
    private lateinit var tvCategoryName: TextView
    private lateinit var ivCategoryIcon: ImageView
    
    private lateinit var tvNote: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView

    private var transaction: TransactionModel? = null
    private val moneyFormat = DecimalFormat("#,###")

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data?.getBooleanExtra("ACTION_RELOAD", false) == true) {
                // Chuyển cờ ACTION_RELOAD về màn History và đóng màn Detail
                setResult(RESULT_OK, data)
                finish()
            }
        }
    }

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
        layoutHeader = findViewById(R.id.layoutHeader)
        
        tvType = findViewById(R.id.tvType)
        tvAmount = findViewById(R.id.tvAmount)
        
        tvWalletName = findViewById(R.id.tvWalletName)
        tvWalletType = findViewById(R.id.tvWalletType)
        ivWalletIcon = findViewById(R.id.ivWalletIcon)
        
        tvCategoryName = findViewById(R.id.tvCategoryName)
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon)
        
        tvNote = findViewById(R.id.tvNote)
        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)

        parseIntentData()
        bindData()
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        btnEdit.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_DATA", Gson().toJson(transaction))
            editLauncher.launch(intent)
        }

        btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa") { _, _ ->
                    // Return to History with delete flag
                    val intent = Intent()
                    intent.putExtra("ACTION_DELETE", true)
                    intent.putExtra("TRANSACTION_ID", transaction?.id)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun parseIntentData() {
        val transactionJson = intent.getStringExtra("TRANSACTION_DATA")
        if (!transactionJson.isNullOrEmpty()) {
            transaction = Gson().fromJson(transactionJson, TransactionModel::class.java)
        }
    }

    private fun bindData() {
        val tx = transaction ?: return

        // Wallet
        tvWalletName.text = tx.wallet.name
        
        val typeName = when (tx.wallet.type.name) {
            "CASH" -> "Tiền mặt"
            "BANK_ACCOUNT" -> "Tài khoản ngân hàng"
            "E_WALLET" -> "Ví điện tử"
            else -> tx.wallet.type.name
        }
        tvWalletType.text = typeName
        // Setup Icon for wallet if needed, e.g. depending on type
        // ivWalletIcon.setImageResource(tx.wallet.type.iconId) // Just using default icon for now

        // Category
        tvCategoryName.text = tx.category.name
        
        // Note
        if (tx.note.isNullOrEmpty()) {
            tvNote.text = "Không có ghi chú"
        } else {
            tvNote.text = tx.note
        }

        // Time
        val (dateStr, timeStr) = formatDateTime(tx.transactionDate)
        tvDate.text = dateStr
        tvTime.text = timeStr

        // Header Background & Text
        if (tx.category.isIncome) {
            layoutHeader.setBackgroundResource(R.drawable.bg_gradient_green)
            tvType.text = "Thu nhập"
            tvAmount.text = "+${moneyFormat.format(tx.amount)}"
        } else {
            // Using a red gradient or red color for expense would be nice, but to match exactly
            // the user provided green gradient in Figma for an expense of -100.000.
            // Let's keep the green background as in the design or set conditional if necessary.
            layoutHeader.setBackgroundResource(R.drawable.bg_gradient_green)
            tvType.text = "Chi tiêu"
            tvAmount.text = "-${moneyFormat.format(tx.amount)}"
        }
    }

    private fun formatDateTime(isoString: String): Pair<String, String> {
        try {
            val date = DateTimeUtils.parseIsoDate(isoString)
            if (date != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = date
                val dayOfWeekInt = cal.get(java.util.Calendar.DAY_OF_WEEK)
                val dayStr = when(dayOfWeekInt) {
                    java.util.Calendar.MONDAY -> "Thứ Hai"
                    java.util.Calendar.TUESDAY -> "Thứ Ba"
                    java.util.Calendar.WEDNESDAY -> "Thứ Tư"
                    java.util.Calendar.THURSDAY -> "Thứ Năm"
                    java.util.Calendar.FRIDAY -> "Thứ Sáu"
                    java.util.Calendar.SATURDAY -> "Thứ Bảy"
                    java.util.Calendar.SUNDAY -> "Chủ Nhật"
                    else -> ""
                }
                
                val datePart = java.text.SimpleDateFormat("dd 'tháng' MM, yyyy", java.util.Locale.getDefault()).format(date)
                val timePart = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
                
                return Pair("$dayStr, $datePart", timePart)
            }
        } catch (e: Exception) {}
        return Pair("Ngày không xác định", "--:--")
    }
}
