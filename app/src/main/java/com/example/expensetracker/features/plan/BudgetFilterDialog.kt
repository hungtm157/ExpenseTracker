package com.example.expensetracker.features.plan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.expensetracker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * BudgetFilterDialog — Bottom Sheet cho phép lọc danh sách ngân sách theo khoảng thời gian.
 */
class BudgetFilterDialog : BottomSheetDialogFragment() {

    interface OnFilterApplied {
        fun onFilterApplied(fromDate: String?, toDate: String?)
    }

    private var listener: OnFilterApplied? = null
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun setOnFilterAppliedListener(listener: OnFilterApplied) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_budget_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvFromDate: TextView = view.findViewById(R.id.tvFromDate)
        val tvToDate: TextView = view.findViewById(R.id.tvToDate)
        val btnReset: TextView = view.findViewById(R.id.btnReset)
        val btnApply: TextView = view.findViewById(R.id.btnApply)

        tvFromDate.setOnClickListener {
            showDatePicker { cal ->
                fromDate = cal
                tvFromDate.text = dateFormat.format(cal.time)
                tvFromDate.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }

        tvToDate.setOnClickListener {
            showDatePicker { cal ->
                toDate = cal
                tvToDate.text = dateFormat.format(cal.time)
                tvToDate.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }

        btnReset.setOnClickListener {
            fromDate = null
            toDate = null
            listener?.onFilterApplied(null, null)
            dismiss()
        }

        btnApply.setOnClickListener {
            val from = fromDate?.let { apiDateFormat.format(it.time) }
            val to = toDate?.let { apiDateFormat.format(it.time) }
            listener?.onFilterApplied(from, to)
            dismiss()
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                onDateSelected(selected)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
