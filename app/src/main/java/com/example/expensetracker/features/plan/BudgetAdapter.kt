package com.example.expensetracker.features.plan

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import java.text.DecimalFormat

/**
 * BudgetAdapter — Adapter cho RecyclerView hiển thị danh sách kế hoạch ngân sách.
 */
class BudgetAdapter(
    private var budgets: List<BudgetModel>,
    private val onItemClick: (BudgetModel) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###")

    // Colors for progress bar states
    private val COLOR_GREEN = Color.parseColor("#00C896")
    private val COLOR_ORANGE = Color.parseColor("#FF9800")
    private val COLOR_RED = Color.parseColor("#E53935")

    private val COLOR_GREEN_BG = Color.parseColor("#E8F5E9")
    private val COLOR_ORANGE_BG = Color.parseColor("#FFF3E0")
    private val COLOR_RED_BG = Color.parseColor("#FFEBEE")

    fun updateData(newBudgets: List<BudgetModel>) {
        this.budgets = newBudgets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        holder.bind(budget)
    }

    override fun getItemCount(): Int = budgets.size

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvAmountLimit: TextView = itemView.findViewById(R.id.tvAmountLimit)
        private val progressBudget: ProgressBar = itemView.findViewById(R.id.progressBudget)
        private val tvSpentInfo: TextView = itemView.findViewById(R.id.tvSpentInfo)

        fun bind(budget: BudgetModel) {
            // Category name
            tvCategoryName.text = budget.category?.name ?: "Danh mục #${budget.categoryId}"

            // Amount limit
            tvAmountLimit.text = "đ${decimalFormat.format(budget.amountLimit)}"

            // Progress and spent info
            val spent = budget.currentSpent ?: 0.0
            val ratio = budget.percentageUsed ?: 0.0
            val percentDisplay = (ratio * 100).toInt()
            val progressValue = percentDisplay.coerceIn(0, 100)

            // Determine color based on threshold
            val threshold = budget.alertThreshold  // e.g. 0.72
            val progressColor: Int
            val bgColor: Int
            val textColor: Int

            when {
                ratio >= 1.0 -> {
                    // Over budget — RED
                    progressColor = COLOR_RED
                    bgColor = COLOR_RED_BG
                    textColor = COLOR_RED
                }
                ratio >= threshold -> {
                    // Near threshold — ORANGE
                    progressColor = COLOR_ORANGE
                    bgColor = COLOR_ORANGE_BG
                    textColor = COLOR_ORANGE
                }
                else -> {
                    // Normal — GREEN
                    progressColor = COLOR_GREEN
                    bgColor = COLOR_GREEN_BG
                    textColor = COLOR_GREEN
                }
            }

            // Apply progress bar colors
            progressBudget.progress = progressValue
            progressBudget.progressTintList = ColorStateList.valueOf(progressColor)
            progressBudget.progressBackgroundTintList = ColorStateList.valueOf(bgColor)

            // Spent info text with color
            tvSpentInfo.text = "Đã chi: ${decimalFormat.format(spent)}đ ($percentDisplay%)"
            tvSpentInfo.setTextColor(textColor)

            // Category icon fallback
            ivCategoryIcon.setImageResource(R.drawable.ic_statistics)
            ivCategoryIcon.imageTintList = null

            // Click listener
            itemView.setOnClickListener { 
                onItemClick(budget) 
            }
        }
    }
}
