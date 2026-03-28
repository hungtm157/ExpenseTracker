package com.example.expensetracker.features.plan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.expensetracker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * BudgetFilterDialog — Bottom Sheet cho phép chọn Năm -> Tháng, Tuần hoặc Tùy chọn để lọc kế hoạch.
 */
class BudgetFilterDialog : BottomSheetDialogFragment() {

    interface OnFilterApplied {
        fun onFilterApplied(fromDate: String?, toDate: String?, label: String)
        fun onReset()
        fun onClear()
    }

    companion object {
        const val MODE_MONTH = 0
        const val MODE_WEEK = 1
        const val MODE_CUSTOM = 2
    }

    private var listener: OnFilterApplied? = null
    
    private var currentMode = MODE_MONTH
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
    private var selectedWeek = 1 // 1-5
    private var customStartDate: Calendar? = null
    private var customEndDate: Calendar? = null

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun setOnFilterAppliedListener(listener: OnFilterApplied) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_budget_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mode buttons
        val btnModeMonth: TextView = view.findViewById(R.id.btnModeMonth)
        val btnModeWeek: TextView = view.findViewById(R.id.btnModeWeek)
        val btnModeCustom: TextView = view.findViewById(R.id.btnModeCustom)

        // Sections
        val layoutYearSelector: LinearLayout = view.findViewById(R.id.layoutYearSelector)
        val gridMonths: GridLayout = view.findViewById(R.id.gridMonths)
        val gridWeeks: GridLayout = view.findViewById(R.id.gridWeeks)
        val layoutCustom: LinearLayout = view.findViewById(R.id.layoutCustom)

        // Elements
        val btnPrevYear: ImageView = view.findViewById(R.id.btnPrevYear)
        val btnNextYear: ImageView = view.findViewById(R.id.btnNextYear)
        val tvSelectedYear: TextView = view.findViewById(R.id.tvSelectedYear)
        val tvStartDate: TextView = view.findViewById(R.id.tvStartDate)
        val tvEndDate: TextView = view.findViewById(R.id.tvEndDate)

        // Footer buttons
        val btnClear: TextView = view.findViewById(R.id.btnClear)
        val btnReset: TextView = view.findViewById(R.id.btnReset)
        val btnApply: TextView = view.findViewById(R.id.btnApply)

        // Initialize UI
        tvSelectedYear.text = selectedYear.toString()
        updateModeUI(btnModeMonth, btnModeWeek, btnModeCustom, layoutYearSelector, gridMonths, gridWeeks, layoutCustom)

        // Mode Switching
        btnModeMonth.setOnClickListener {
            currentMode = MODE_MONTH
            updateModeUI(btnModeMonth, btnModeWeek, btnModeCustom, layoutYearSelector, gridMonths, gridWeeks, layoutCustom)
        }
        btnModeWeek.setOnClickListener {
            currentMode = MODE_WEEK
            updateModeUI(btnModeMonth, btnModeWeek, btnModeCustom, layoutYearSelector, gridMonths, gridWeeks, layoutCustom)
        }
        btnModeCustom.setOnClickListener {
            currentMode = MODE_CUSTOM
            updateModeUI(btnModeMonth, btnModeWeek, btnModeCustom, layoutYearSelector, gridMonths, gridWeeks, layoutCustom)
        }

        // Year logic
        btnPrevYear.setOnClickListener {
            selectedYear--
            tvSelectedYear.text = selectedYear.toString()
        }
        btnNextYear.setOnClickListener {
            selectedYear++
            tvSelectedYear.text = selectedYear.toString()
        }

        // Month Selection
        val monthViews = mutableListOf<TextView>()
        for (i in 0 until gridMonths.childCount) {
            val child = gridMonths.getChildAt(i) as TextView
            monthViews.add(child)
            val monthValue = child.tag.toString().toInt()
            updateItemHighlight(child, monthValue == selectedMonth)
            child.setOnClickListener {
                selectedMonth = monthValue
                monthViews.forEach { mv -> updateItemHighlight(mv, mv.tag.toString().toInt() == selectedMonth) }
            }
        }

        // Week Selection
        val weekViews = mutableListOf<TextView>()
        for (i in 0 until gridWeeks.childCount) {
            val child = gridWeeks.getChildAt(i) as TextView
            weekViews.add(child)
            val weekValue = child.tag.toString().toInt()
            updateItemHighlight(child, weekValue == selectedWeek)
            child.setOnClickListener {
                selectedWeek = weekValue
                weekViews.forEach { wv -> updateItemHighlight(wv, wv.tag.toString().toInt() == selectedWeek) }
            }
        }

        // Custom Range Logic
        tvStartDate.setOnClickListener {
            showDatePicker { cal ->
                customStartDate = cal
                tvStartDate.text = "Từ: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time)}"
            }
        }
        tvEndDate.setOnClickListener {
            showDatePicker { cal ->
                customEndDate = cal
                tvEndDate.text = "Đến: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time)}"
            }
        }

        // Footer Actions
        btnClear.setOnClickListener {
            listener?.onClear()
            dismiss()
        }

        btnReset.setOnClickListener {
            listener?.onReset()
            dismiss()
        }

        btnApply.setOnClickListener {
            applyFilter()
        }
    }

    private fun updateModeUI(
        btnMonth: TextView, btnWeek: TextView, btnCustom: TextView,
        layoutYear: View, gridMonths: View, gridWeeks: View, layoutCustom: View
    ) {
        val selectedBg = R.drawable.bg_month_selected
        val unselectedBg = 0
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        btnMonth.setBackgroundResource(if (currentMode == MODE_MONTH) selectedBg else unselectedBg)
        btnMonth.setTextColor(if (currentMode == MODE_MONTH) selectedColor else unselectedColor)
        btnMonth.setTypeface(null, if (currentMode == MODE_MONTH) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        btnWeek.setBackgroundResource(if (currentMode == MODE_WEEK) selectedBg else unselectedBg)
        btnWeek.setTextColor(if (currentMode == MODE_WEEK) selectedColor else unselectedColor)
        btnWeek.setTypeface(null, if (currentMode == MODE_WEEK) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        btnCustom.setBackgroundResource(if (currentMode == MODE_CUSTOM) selectedBg else unselectedBg)
        btnCustom.setTextColor(if (currentMode == MODE_CUSTOM) selectedColor else unselectedColor)
        btnCustom.setTypeface(null, if (currentMode == MODE_CUSTOM) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)

        layoutYear.visibility = if (currentMode == MODE_CUSTOM) View.GONE else View.VISIBLE
        gridMonths.visibility = if (currentMode == MODE_MONTH) View.VISIBLE else View.GONE
        gridWeeks.visibility = if (currentMode == MODE_WEEK) View.VISIBLE else View.GONE
        layoutCustom.visibility = if (currentMode == MODE_CUSTOM) View.VISIBLE else View.GONE
    }

    private fun updateItemHighlight(view: TextView, isSelected: Boolean) {
        if (isSelected) {
            view.setBackgroundResource(R.drawable.bg_month_selected)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            view.setBackgroundResource(R.drawable.bg_input_rounded)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val result = Calendar.getInstance()
            result.set(year, month, day)
            onDateSelected(result)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun applyFilter() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        var fromDate: String? = null
        var toDate: String? = null
        var label = ""

        when (currentMode) {
            MODE_MONTH -> {
                cal.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                fromDate = sdf.format(cal.time)
                
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                toDate = sdf.format(cal.time)
                label = "T$selectedMonth/$selectedYear"
            }
            MODE_WEEK -> {
                // Simplified week calculation: 1-7, 8-14, 15-21, 22-28, 29-end
                val startDay = (selectedWeek - 1) * 7 + 1
                cal.set(selectedYear, selectedMonth - 1, startDay, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                fromDate = sdf.format(cal.time)

                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                var endDay = startDay + 6
                if (endDay > maxDay || selectedWeek == 5) endDay = maxDay
                
                cal.set(Calendar.DAY_OF_MONTH, endDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                toDate = sdf.format(cal.time)
                label = "Tuần $selectedWeek, T$selectedMonth"
            }
            MODE_CUSTOM -> {
                if (customStartDate == null || customEndDate == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show()
                    return
                }
                if (customEndDate!!.before(customStartDate)) {
                    Toast.makeText(requireContext(), "Ngày kết thúc không được nhỏ hơn ngày bắt đầu", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // Convert to UTC for consistency
                val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = customStartDate!!.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = customEndDate!!.timeInMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                fromDate = sdf.format(startCal.time)
                toDate = sdf.format(endCal.time)
                
                val labelSdf = SimpleDateFormat("dd/MM", Locale.US)
                label = "${labelSdf.format(customStartDate!!.time)} - ${labelSdf.format(customEndDate!!.time)}"
            }
        }

        listener?.onFilterApplied(fromDate, toDate, label)
        dismiss()
    }
}

