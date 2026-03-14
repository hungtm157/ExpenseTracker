package com.example.expensetracker.features.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.utils.DateUtils
import com.example.expensetracker.core.utils.toCurrencyString
import com.example.expensetracker.data.models.Expense

/**
 * HomeAdapter — RecyclerView Adapter hiển thị danh sách chi tiêu.
 */
class HomeAdapter(
    private val items: MutableList<Expense> = mutableListOf(),
    private val onItemClick: (Expense) -> Unit = {}
) : RecyclerView.Adapter<HomeAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = items[position]
        holder.tvTitle.text = expense.title
        holder.tvAmount.text = expense.amount.toCurrencyString()
        holder.tvDate.text = DateUtils.formatDate(expense.date)
        holder.tvCategory.text = expense.category
        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount(): Int = items.size

    /** Cập nhật toàn bộ danh sách */
    fun updateData(newItems: List<Expense>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
