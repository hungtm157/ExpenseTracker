package com.example.expensetracker.features.statistics

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseFragment
import com.example.expensetracker.core.network.ApiClient
import com.example.expensetracker.core.network.ApiService
import com.example.expensetracker.core.utils.CustomPieChartView
import com.example.expensetracker.data.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * StatisticsFragment — Màn hình Thống kê (tab thứ hai trong Bottom Navigation).
 */
class StatisticsFragment : BaseFragment(R.layout.fragment_statistics) {

    // UI elements
    private lateinit var tabMonth: TextView
    private lateinit var tabYear: TextView
    private lateinit var btnPrev: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var tvDateLabel: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tabCatExpense: TextView
    private lateinit var tabCatIncome: TextView
    private lateinit var pieChart: CustomPieChartView
    private lateinit var llCategoryListContainer: LinearLayout

    // State
    private var timeMode = "MONTH" // MONTH or YEAR
    private var currentDate = Calendar.getInstance()
    private var categoryType = "EXPENSE"

    // Predefined colors for pie chart slices
    private val sliceColors = intArrayOf(
        Color.parseColor("#4CAF50"), // green
        Color.parseColor("#2196F3"), // blue
        Color.parseColor("#FF9800"), // orange
        Color.parseColor("#E91E63"), // pink
        Color.parseColor("#9C27B0"), // purple
        Color.parseColor("#00BCD4"), // cyan
        Color.parseColor("#795548"), // brown
        Color.parseColor("#607D8B"), // blue grey
        Color.parseColor("#FF5722"), // deep orange
        Color.parseColor("#3F51B5"), // indigo
    )

    override fun initViews(view: View) {
        tabMonth = view.findViewById(R.id.tabMonth)
        tabYear = view.findViewById(R.id.tabYear)
        btnPrev = view.findViewById(R.id.btnPrev)
        btnNext = view.findViewById(R.id.btnNext)
        tvDateLabel = view.findViewById(R.id.tvDateLabel)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tabCatExpense = view.findViewById(R.id.tabCatExpense)
        tabCatIncome = view.findViewById(R.id.tabCatIncome)
        pieChart = view.findViewById(R.id.pieChart)
        llCategoryListContainer = view.findViewById(R.id.llCategoryListContainer)

        updateDateLabel()
        loadStatistics()
    }

    override fun initListeners() {
        tabMonth.setOnClickListener { selectTimeMode("MONTH") }
        tabYear.setOnClickListener { selectTimeMode("YEAR") }

        btnPrev.setOnClickListener {
            if (timeMode == "MONTH") {
                currentDate.add(Calendar.MONTH, -1)
            } else {
                currentDate.add(Calendar.YEAR, -1)
            }
            updateDateLabel()
            loadStatistics()
        }

        btnNext.setOnClickListener {
            if (timeMode == "MONTH") {
                currentDate.add(Calendar.MONTH, 1)
            } else {
                currentDate.add(Calendar.YEAR, 1)
            }
            updateDateLabel()
            loadStatistics()
        }

        tabCatExpense.setOnClickListener { selectCategoryType("EXPENSE") }
        tabCatIncome.setOnClickListener { selectCategoryType("INCOME") }
    }

    private fun selectTimeMode(mode: String) {
        timeMode = mode
        if (mode == "MONTH") {
            tabMonth.setBackgroundResource(R.drawable.bg_chip_selected)
            tabMonth.setTextColor(resources.getColor(R.color.green_mid, null))
            tabMonth.paint.isFakeBoldText = true
            tabYear.setBackgroundColor(Color.TRANSPARENT)
            tabYear.setTextColor(Color.WHITE)
            tabYear.paint.isFakeBoldText = false
        } else {
            tabYear.setBackgroundResource(R.drawable.bg_chip_selected)
            tabYear.setTextColor(resources.getColor(R.color.green_mid, null))
            tabYear.paint.isFakeBoldText = true
            tabMonth.setBackgroundColor(Color.TRANSPARENT)
            tabMonth.setTextColor(Color.WHITE)
            tabMonth.paint.isFakeBoldText = false
        }
        updateDateLabel()
        loadStatistics()
    }

    private fun selectCategoryType(type: String) {
        categoryType = type
        if (type == "EXPENSE") {
            tabCatExpense.setBackgroundResource(R.drawable.bg_chip_selected)
            tabCatExpense.setTextColor(resources.getColor(R.color.text_primary, null))
            tabCatExpense.paint.isFakeBoldText = true
            tabCatIncome.setBackgroundColor(Color.TRANSPARENT)
            tabCatIncome.setTextColor(resources.getColor(R.color.text_hint, null))
            tabCatIncome.paint.isFakeBoldText = false
        } else {
            tabCatIncome.setBackgroundResource(R.drawable.bg_chip_selected)
            tabCatIncome.setTextColor(resources.getColor(R.color.text_primary, null))
            tabCatIncome.paint.isFakeBoldText = true
            tabCatExpense.setBackgroundColor(Color.TRANSPARENT)
            tabCatExpense.setTextColor(resources.getColor(R.color.text_hint, null))
            tabCatExpense.paint.isFakeBoldText = false
        }
        loadStatistics()
    }

    private fun updateDateLabel() {
        if (timeMode == "MONTH") {
            val fmt = SimpleDateFormat("'Tháng' M/yyyy", Locale("vi", "VN"))
            tvDateLabel.text = fmt.format(currentDate.time)
        } else {
            val fmt = SimpleDateFormat("'Năm' yyyy", Locale("vi", "VN"))
            tvDateLabel.text = fmt.format(currentDate.time)
        }
    }

    private fun getDateRange(): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

        val from = Calendar.getInstance().apply { time = currentDate.time }
        val to = Calendar.getInstance().apply { time = currentDate.time }

        if (timeMode == "MONTH") {
            from.set(Calendar.DAY_OF_MONTH, 1)
            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH))
            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)
        } else {
            from.set(Calendar.MONTH, Calendar.JANUARY)
            from.set(Calendar.DAY_OF_MONTH, 1)
            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.MONTH, Calendar.DECEMBER)
            to.set(Calendar.DAY_OF_MONTH, 31)
            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)
        }

        return Pair(sdf.format(from.time), sdf.format(to.time))
    }

    private fun loadStatistics() {
        val apiService = ApiClient.create(ApiService::class.java)
        val statsRepo = StatisticsRepository(apiService)
        val numberFormat = NumberFormat.getInstance(Locale("vi", "VN"))
        val (fromDate, toDate) = getDateRange()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. General Statistics
                val generalResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsGeneral(fromDate, toDate)
                }
                if (generalResult.isSuccessful) {
                    val data = generalResult.body()?.data
                    if (data != null) {
                        tvTotalExpense.text = numberFormat.format(data.totalExpense)
                        tvTotalIncome.text = numberFormat.format(data.totalIncome)
                    }
                }

                // 2. Statistics By Category
                val categoryResult = withContext(Dispatchers.IO) {
                    statsRepo.getStatisticsByCategory(categoryType, fromDate, toDate)
                }
                if (categoryResult.isSuccessful) {
                    val catList = categoryResult.body()?.data ?: emptyList()
                    val totalAmount = catList.sumOf { it.totalAmount }

                    // Build pie chart data
                    val pieData = catList.mapIndexed { index, item ->
                        val color = sliceColors[index % sliceColors.size]
                        Pair(item.totalAmount.toFloat(), color)
                    }
                    pieChart.setData(pieData)

                    // Build category list
                    llCategoryListContainer.removeAllViews()
                    catList.forEachIndexed { index, item ->
                        val itemView = layoutInflater.inflate(
                            R.layout.item_statistics_category,
                            llCategoryListContainer,
                            false
                        )
                        val vColor = itemView.findViewById<View>(R.id.vColorIndicator)
                        val tvName = itemView.findViewById<TextView>(R.id.tvCatName)
                        val tvAmount = itemView.findViewById<TextView>(R.id.tvCatAmount)
                        val tvPercent = itemView.findViewById<TextView>(R.id.tvCatPercent)

                        val color = sliceColors[index % sliceColors.size]
                        val bg = vColor.background.mutate() as GradientDrawable
                        bg.setColor(color)
                        vColor.background = bg

                        tvName.text = item.categoryName
                        tvAmount.text = numberFormat.format(item.totalAmount)
                        val pct = if (totalAmount > 0) {
                            (item.totalAmount.toDouble() / totalAmount * 100)
                        } else 0.0
                        tvPercent.text = String.format(Locale.US, "%.1f%%", pct)

                        llCategoryListContainer.addView(itemView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
