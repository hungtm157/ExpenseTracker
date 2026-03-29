package com.example.expensetracker.features.plan

import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.data.models.CategoryItem
import com.example.expensetracker.data.repository.BudgetRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddBudgetActivity — Màn hình tạo/sửa kế hoạch ngân sách.
 */
class AddBudgetActivity : BaseActivity(R.layout.activity_add_budget), BudgetListener {

    private lateinit var btnBack: ImageView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmountLimit: EditText
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var btnThisWeek: TextView
    private lateinit var btnThisMonth: TextView
    private lateinit var btnThisYear: TextView
    private lateinit var switchAlert: SwitchMaterial
    private lateinit var layoutThreshold: LinearLayout
    private lateinit var seekBarThreshold: SeekBar
    private lateinit var tvThresholdValue: TextView
    private lateinit var tvThresholdHint: TextView
    private lateinit var btnCancel: TextView
    private lateinit var btnCreate: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var controller: BudgetController
    private val scope = CoroutineScope(Dispatchers.Main)

    private var categories = mutableListOf<CategoryItem>()
    private var selectedCategoryId: Int = -1
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    // Edit mode
    private var editBudgetId: Int = -1
    private var isEditMode = false

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val parseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    private val amountWatcher = object : TextWatcher {
        private var current = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val input = s.toString()
            if (input != current) {
                etAmountLimit.removeTextChangedListener(this)

                val cleanString = input.replace(",", "")
                if (cleanString.isNotEmpty()) {
                    try {
                        val parsed = cleanString.toDouble()
                        val formatted = decimalFormat.format(parsed)

                        current = formatted
                        etAmountLimit.setText(formatted)
                        etAmountLimit.setSelection(formatted.length)
                    } catch (e: Exception) {
                        current = ""
                    }
                } else {
                    current = ""
                }

                etAmountLimit.addTextChangedListener(this)
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    override fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAmountLimit = findViewById(R.id.etAmountLimit)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        btnThisWeek = findViewById(R.id.btnThisWeek)
        btnThisMonth = findViewById(R.id.btnThisMonth)
        btnThisYear = findViewById(R.id.btnThisYear)
        switchAlert = findViewById(R.id.switchAlert)
        layoutThreshold = findViewById(R.id.layoutThreshold)
        seekBarThreshold = findViewById(R.id.seekBarThreshold)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        tvThresholdHint = findViewById(R.id.tvThresholdHint)
        btnCancel = findViewById(R.id.btnCancel)
        btnCreate = findViewById(R.id.btnCreate)
        progressBar = findViewById(R.id.progressBar)

        // Init controller
        val apiService = ApiClient.create(ApiService::class.java)
        val repository = BudgetRepository(apiService)
        controller = BudgetController(repository, this)

        apiDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        parseDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Check edit mode
        editBudgetId = intent.getIntExtra("budget_id", -1)
        isEditMode = editBudgetId > 0

        if (isEditMode) {
            tvHeaderTitle.text = "Sửa kế hoạch ngân sách"
            btnCreate.text = "Cập nhật"

            // Pre-fill form with existing data
            val amountLimit = intent.getDoubleExtra("amount_limit", 0.0)
            etAmountLimit.setText(decimalFormat.format(amountLimit))

            val isAlertEnabled = intent.getBooleanExtra("is_alert_enabled", true)
            switchAlert.isChecked = isAlertEnabled

            val threshold = intent.getDoubleExtra("alert_threshold", 0.8)
            val thresholdPercent = (threshold * 100).toInt()
            seekBarThreshold.progress = thresholdPercent
            tvThresholdValue.text = "$thresholdPercent%"
            tvThresholdHint.text = "Bạn sẽ nhận cảnh báo khi chi tiêu đạt $thresholdPercent% hạn mức"

            // Parse dates
            val startDateStr = intent.getStringExtra("start_date")
            val endDateStr = intent.getStringExtra("end_date")
            if (startDateStr != null) {
                try {
                    val parsed = parseDateFormat.parse(startDateStr.substringBefore("."))
                    if (parsed != null) {
                        startDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            time = parsed
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        tvStartDate.text = dateFormat.format(startDate!!.time)
                    }
                } catch (_: Exception) {}
            }
            if (endDateStr != null) {
                try {
                    val parsed = parseDateFormat.parse(endDateStr.substringBefore("."))
                    if (parsed != null) {
                        endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            time = parsed
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        tvEndDate.text = dateFormat.format(endDate!!.time)
                    }
                } catch (_: Exception) {}
            }

            selectedCategoryId = intent.getIntExtra("category_id", -1)
        }

        // Load categories for spinner
        loadCategories()
    }

    override fun initListeners() {
        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        etAmountLimit.addTextChangedListener(amountWatcher)

        tvStartDate.setOnClickListener { showDatePicker(true) }
        tvEndDate.setOnClickListener { showDatePicker(false) }

        btnThisWeek.setOnClickListener { setThisWeek() }
        btnThisMonth.setOnClickListener { setThisMonth() }
        btnThisYear.setOnClickListener { setThisYear() }

        switchAlert.setOnCheckedChangeListener { _, isChecked ->
            layoutThreshold.visibility = if (isChecked) View.VISIBLE else View.GONE
            seekBarThreshold.visibility = if (isChecked) View.VISIBLE else View.GONE
            tvThresholdHint.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        seekBarThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvThresholdValue.text = "$progress%"
                tvThresholdHint.text = "Bạn sẽ nhận cảnh báo khi chi tiêu đạt $progress% hạn mức"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnCreate.setOnClickListener { submitBudget() }
    }

    private fun loadCategories() {
        scope.launch {
            try {
                val apiService = ApiClient.create(ApiService::class.java)
                val response = withContext(Dispatchers.IO) {
                    apiService.getCategories(page = 1, limit = 100, type = "EXPENSE")
                }
                if (response.isSuccessful) {
                    val items = response.body()?.data?.items ?: emptyList()
                    categories.clear()
                    categories.addAll(items.filter { it.isActive })

                    val names = categories.map { it.name }
                    val adapter = ArrayAdapter(
                        this@AddBudgetActivity,
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter

                    spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedCategoryId = categories[position].id
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    // In edit mode, select the correct category
                    if (isEditMode) {
                        val editCategoryId = intent.getIntExtra("category_id", -1)
                        val index = categories.indexOfFirst { it.id == editCategoryId }
                        if (index >= 0) {
                            spinnerCategory.setSelection(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddBudgetActivity, "Không tải được danh mục", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = if (isStart && startDate != null) startDate!!
                  else if (!isStart && endDate != null) endDate!!
                  else Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    if (isStart) {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    } else {
                        set(year, month, day, 23, 59, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                }
                if (isStart) {
                    startDate = selected
                    tvStartDate.text = dateFormat.format(selected.time)
                } else {
                    endDate = selected
                    tvEndDate.text = dateFormat.format(selected.time)
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setThisWeek() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        startDate = cal.clone() as Calendar
        tvStartDate.text = dateFormat.format(startDate!!.time)

        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        endDate = cal.clone() as Calendar
        tvEndDate.text = dateFormat.format(endDate!!.time)
    }

    private fun setThisMonth() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = cal.clone() as Calendar
        tvStartDate.text = dateFormat.format(startDate!!.time)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        endDate = cal.clone() as Calendar
        tvEndDate.text = dateFormat.format(endDate!!.time)
    }

    private fun setThisYear() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = cal.clone() as Calendar
        tvStartDate.text = dateFormat.format(startDate!!.time)

        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        endDate = cal.clone() as Calendar
        tvEndDate.text = dateFormat.format(endDate!!.time)
    }

    private fun submitBudget() {
        // Validate
        if (selectedCategoryId <= 0) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
            return
        }

        val amountStr = etAmountLimit.text.toString().trim().replace(",", "")
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Vui lòng nhập hạn mức chi tiêu hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show()
            return
        }

        val threshold = seekBarThreshold.progress / 100.0

        if (isEditMode) {
            // Update existing budget
            val request = BudgetUpdateRequest(
                categoryId = selectedCategoryId,
                amountLimit = amount,
                startDate = apiDateFormat.format(startDate!!.time),
                endDate = apiDateFormat.format(endDate!!.time),
                isAlertEnabled = switchAlert.isChecked,
                alertThreshold = threshold
            )
            controller.updateBudget(editBudgetId, request)
        } else {
            // Create new budget
            val request = BudgetCreateRequest(
                categoryId = selectedCategoryId,
                amountLimit = amount,
                startDate = apiDateFormat.format(startDate!!.time),
                endDate = apiDateFormat.format(endDate!!.time),
                isAlertEnabled = switchAlert.isChecked,
                alertThreshold = threshold
            )
            controller.createBudget(request)
        }
    }

    // ─── BudgetListener Callbacks ────────────────────────────────────────────

    override fun onBudgetsLoaded(budgets: List<BudgetModel>) {
        // Not used in this activity
    }

    override fun onBudgetDetailLoaded(budget: BudgetModel) {
        // Not used in this activity
    }

    override fun onBudgetCreated(budget: BudgetModel) {
        Toast.makeText(this, "Đã tạo kế hoạch ngân sách thành công!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onBudgetUpdated(budget: BudgetModel) {
        Toast.makeText(this, "Đã cập nhật kế hoạch ngân sách thành công!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onBudgetCompleted(budget: BudgetModel) {
        // Not used in this activity
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnCreate.isEnabled = !isLoading
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
