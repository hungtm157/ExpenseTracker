package com.example.expensetracker.features.transaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.features.wallet.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class TransactionHistoryActivity : BaseActivity(R.layout.activity_transaction_history), TransactionHistoryListener {

    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var chipAll: TextView
    private lateinit var chipIncome: TextView
    private lateinit var chipExpense: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var tvEmptyState: android.widget.TextView

    private lateinit var repository: TransactionRepository
    private lateinit var adapter: TransactionHistoryAdapter
    private lateinit var controller: TransactionHistoryController
    private val moneyFormat = DecimalFormat("#,###")

    private var allTransactions = mutableListOf<TransactionModel>()
    private var currentType: String? = null

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        chipAll = findViewById(R.id.chipAll)
        chipIncome = findViewById(R.id.chipIncome)
        chipExpense = findViewById(R.id.chipExpense)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        rvTransactions = findViewById(R.id.rvTransactions)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        val apiService = ApiClient.create(ApiService::class.java)
        repository = TransactionRepository(apiService)
        controller = TransactionHistoryController(this, this)

        adapter = TransactionHistoryAdapter(mutableListOf()) { transaction ->
            deleteTransaction(transaction)
        }
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter

        // Set default chip selection
        selectChip(chipAll)
        currentType = null
        
        // Fetch data
        controller.fetchTransactions(currentType)
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        chipAll.setOnClickListener { 
            selectChip(chipAll)
            currentType = null
            controller.fetchTransactions(currentType)
        }
        chipIncome.setOnClickListener { 
            selectChip(chipIncome) 
            currentType = "INCOME"
            controller.fetchTransactions(currentType)
        }
        chipExpense.setOnClickListener { 
            selectChip(chipExpense) 
            currentType = "EXPENSE"
            controller.fetchTransactions(currentType)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun selectChip(selectedChip: TextView) {
        val chips = listOf(chipAll, chipIncome, chipExpense)
        for (chip in chips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_chip_white)
                chip.setTextColor(getColor(R.color.green_mid))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_green_trans)
                chip.setTextColor(getColor(R.color.white))
            }
        }
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().trim()
        val filtered = allTransactions.filter { 
            query.isEmpty() || it.note?.contains(query, ignoreCase = true) == true || it.category.name.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
        
        // Handle empty state
        if (filtered.isEmpty()) {
            tvEmptyState.visibility = android.view.View.VISIBLE
            rvTransactions.visibility = android.view.View.GONE
        } else {
            tvEmptyState.visibility = android.view.View.GONE
            rvTransactions.visibility = android.view.View.VISIBLE
        }
    }

    private fun deleteTransaction(transaction: TransactionModel) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
            .setPositiveButton("Xóa") { _, _ ->
                controller.deleteTransaction(transaction.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        if (isLoading) {
            tvEmptyState.visibility = android.view.View.GONE
            rvTransactions.visibility = android.view.View.GONE
        }
    }

    override fun onTransactionsLoaded(transactions: List<TransactionModel>) {
        allTransactions.clear()
        allTransactions.addAll(transactions)

        // Tính tổng thu, tổng chi
        var totalIn = 0.0
        var totalOut = 0.0
        for (tx in transactions) {
            if (tx.category.isIncome) totalIn += tx.amount
            else totalOut -= tx.amount
        }

        tvTotalIncome.text = "+${moneyFormat.format(totalIn)}"
        tvTotalExpense.text = moneyFormat.format(totalOut)

        applyFilters()
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteSuccess(transactionId: Int) {
        Toast.makeText(this, "Xóa giao dịch thành công", Toast.LENGTH_SHORT).show()
        controller.fetchTransactions(currentType)
    }
}
