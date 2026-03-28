package com.example.expensetracker.features.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.expensetracker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

/**
 * BudgetFilterDialog — Bottom Sheet cho phép chọn Năm -> Tháng để lọc kế hoạch.
 */
class BudgetFilterDialog : BottomSheetDialogFragment() {

    interface OnFilterApplied {
        fun onFilterApplied(month: Int, year: Int)
        fun onReset()
    }

    private var listener: OnFilterApplied? = null
    
    // Lưu trạng thái đang chọn trong Dialog (mặc định là hiện tại)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12

    fun setOnFilterAppliedListener(listener: OnFilterApplied) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_budget_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPrevYear: ImageView = view.findViewById(R.id.btnPrevYear)
        val btnNextYear: ImageView = view.findViewById(R.id.btnNextYear)
        val tvSelectedYear: TextView = view.findViewById(R.id.tvSelectedYear)
        val gridMonths: GridLayout = view.findViewById(R.id.gridMonths)
        val btnReset: TextView = view.findViewById(R.id.btnReset)
        val btnApply: TextView = view.findViewById(R.id.btnApply)

        // Cập nhật text năm ban đầu
        tvSelectedYear.text = selectedYear.toString()

        // Xử lý nút thay đổi năm
        btnPrevYear.setOnClickListener {
            selectedYear--
            tvSelectedYear.text = selectedYear.toString()
        }
        btnNextYear.setOnClickListener {
            selectedYear++
            tvSelectedYear.text = selectedYear.toString()
        }

        // Xử lý chọn tháng từ Grid
        val monthViews = mutableListOf<TextView>()
        for (i in 0 until gridMonths.childCount) {
            val child = gridMonths.getChildAt(i)
            if (child is TextView) {
                monthViews.add(child)
                val monthValue = child.tag.toString().toInt()
                
                // Highlight tháng đang được chọn
                updateMonthHighlight(child, monthValue == selectedMonth)

                child.setOnClickListener {
                    selectedMonth = monthValue
                    // Cập nhật lại UI highlight cho tất cả
                    monthViews.forEach { mv ->
                        val mvVal = mv.tag.toString().toInt()
                        updateMonthHighlight(mv, mvVal == selectedMonth)
                    }
                }
            }
        }

        btnReset.setOnClickListener {
            listener?.onReset()
            dismiss()
        }

        btnApply.setOnClickListener {
            listener?.onFilterApplied(selectedMonth, selectedYear)
            dismiss()
        }
    }

    private fun updateMonthHighlight(view: TextView, isSelected: Boolean) {
        if (isSelected) {
            view.setBackgroundResource(R.drawable.bg_month_selected)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            view.setBackgroundResource(R.drawable.bg_input_rounded)
            view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        }
    }
}
