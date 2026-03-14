package com.example.expensetracker.features.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private lateinit var layoutDateFilter: LinearLayout
    private lateinit var tvDateRange: TextView

    private lateinit var repository: TransactionRepository
    private lateinit var adapter: TransactionHistoryAdapter
    private lateinit var controller: TransactionHistoryController
    private val moneyFormat = DecimalFormat("#,###")

    private var allTransactions = mutableListOf<TransactionModel>()
    private var currentType: String? = null
    private var filterStartDate: String? = null
    private var filterEndDate: String? = null

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
        layoutDateFilter = findViewById(R.id.layoutDateFilter)
        tvDateRange = findViewById(R.id.tvDateRange)

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
        
        // Cài đặt mặc định là Tháng này
        val calendar = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        filterEndDate = isoFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        filterStartDate = isoFormat.format(calendar.time)

        try {
            val startDisp = displayFormat.format(isoFormat.parse(filterStartDate!!)!!)
            val endDisp = displayFormat.format(isoFormat.parse(filterEndDate!!)!!)
            tvDateRange.text = "$startDisp - $endDisp"
        } catch (e: Exception) {
            tvDateRange.text = "$filterStartDate - $filterEndDate"
        }

        // Fetch data
        controller.fetchTransactions(currentType, filterStartDate, filterEndDate)
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }

        layoutDateFilter.setOnClickListener {
            showDateFilterBottomSheet()
        }

        chipAll.setOnClickListener { 
            selectChip(chipAll)
            currentType = null
            controller.fetchTransactions(currentType, filterStartDate, filterEndDate)
        }
        chipIncome.setOnClickListener { 
            selectChip(chipIncome) 
            currentType = "INCOME"
            controller.fetchTransactions(currentType, filterStartDate, filterEndDate)
        }
        chipExpense.setOnClickListener { 
            selectChip(chipExpense) 
            currentType = "EXPENSE"
            controller.fetchTransactions(currentType, filterStartDate, filterEndDate)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showDateFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_date_filter, null)
        bottomSheetDialog.setContentView(view)

        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDate)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDate)
        val chip7Days = view.findViewById<TextView>(R.id.chip7Days)
        val chip30Days = view.findViewById<TextView>(R.id.chip30Days)
        val chipThisMonth = view.findViewById<TextView>(R.id.chipThisMonth)
        val btnApply = view.findViewById<Button>(R.id.btnApply)

        var tempStartDate = filterStartDate
        var tempEndDate = filterEndDate

        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun updateDateViews() {
            try {
                tvStartDate.text = tempStartDate?.let { displayFormat.format(isoFormat.parse(it)!!) } ?: ""
                tvEndDate.text = tempEndDate?.let { displayFormat.format(isoFormat.parse(it)!!) } ?: ""
            } catch (e: Exception) {
                tvStartDate.text = tempStartDate ?: ""
                tvEndDate.text = tempEndDate ?: ""
            }
        }
        updateDateViews()

        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        tvStartDate.setOnClickListener {
            showDatePicker(tempStartDate ?: "") { isoDate ->
                tempStartDate = isoDate
                updateDateViews()
            }
        }

        tvEndDate.setOnClickListener {
            showDatePicker(tempEndDate ?: "") { isoDate ->
                tempEndDate = isoDate
                updateDateViews()
            }
        }

        chip7Days.setOnClickListener {
            val calendar = Calendar.getInstance()
            tempEndDate = isoFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            tempStartDate = isoFormat.format(calendar.time)
            updateDateViews()
        }

        chip30Days.setOnClickListener {
            val calendar = Calendar.getInstance()
            tempEndDate = isoFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            tempStartDate = isoFormat.format(calendar.time)
            updateDateViews()
        }

        chipThisMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            tempEndDate = isoFormat.format(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            tempStartDate = isoFormat.format(calendar.time)
            updateDateViews()
        }

        btnApply.setOnClickListener {
            filterStartDate = tempStartDate
            filterEndDate = tempEndDate
            
            if (filterStartDate != null && filterEndDate != null) {
                try {
                    val startDisp = displayFormat.format(isoFormat.parse(filterStartDate!!)!!)
                    val endDisp = displayFormat.format(isoFormat.parse(filterEndDate!!)!!)
                    tvDateRange.text = "$startDisp - $endDisp"
                } catch (e: Exception) {
                     tvDateRange.text = "$filterStartDate - $filterEndDate"
                }
            } else {
                tvDateRange.text = "Tất cả thời gian"
            }
            
            bottomSheetDialog.dismiss()
            controller.fetchTransactions(currentType, filterStartDate, filterEndDate)
        }

        bottomSheetDialog.show()
    }

    private fun showDatePicker(currentIsoDate: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (currentIsoDate.isNotEmpty()) {
            try {
                calendar.time = isoFormat.parse(currentIsoDate)!!
            } catch (e: Exception) {}
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                onDateSelected(isoFormat.format(selectedCalendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
