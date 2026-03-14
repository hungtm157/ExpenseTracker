package com.example.expensetracker.features.transaction

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import java.text.DecimalFormat
import androidx.core.graphics.toColorInt
import com.example.expensetracker.utils.DateTimeUtils

class TransactionHistoryAdapter(
    private var transactions: List<TransactionModel>,
    private val onItemClick: (TransactionModel) -> Unit,
    private val onDeleteClick: (TransactionModel) -> Unit
) : RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder>() {

    private val moneyFormat = DecimalFormat("#,###")

    fun updateData(newTransactions: List<TransactionModel>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_history, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        
        // Show date header if it's the first item or date differs from the previous one
        val showDate = position == 0 || transactions[position - 1].transactionDate != transaction.transactionDate
        holder.layoutDateHeader.visibility = if (showDate) View.VISIBLE else View.GONE
        
        // Cần format lại ngày tháng (tạm thời hiển thị raw string từ model)
        holder.tvDateHeader.text = formatDisplayDate(transaction.transactionDate)

        holder.tvCategoryName.text = transaction.category.name
        holder.tvNote.text = transaction.note ?: ""
        
        holder.tvTime.text = formatTime(transaction.transactionDate)

        val context = holder.itemView.context
        val greenColor = "#00D492".toColorInt()
        val redColor = "#FF5252".toColorInt()
        holder.tvTime.text = DateTimeUtils.getHourMinute(transaction.createdAt)

        if (transaction.category.isIncome) {
             holder.tvAmount.text = "+${moneyFormat.format(transaction.amount)}"
             holder.tvAmount.setTextColor(greenColor)
             holder.ivCategoryIcon.setImageResource(R.drawable.icon_thu)
             holder.ivCategoryIcon.setColorFilter(greenColor)
             holder.ivCategoryIcon.backgroundTintList = ColorStateList.valueOf("#E8F5E9".toColorInt())
        } else {
             holder.tvAmount.text = "-${moneyFormat.format(transaction.amount)}"
             holder.tvAmount.setTextColor(redColor)
             holder.ivCategoryIcon.setImageResource(R.drawable.icon_chi)
             holder.ivCategoryIcon.setColorFilter(redColor)
             holder.ivCategoryIcon.backgroundTintList = ColorStateList.valueOf("#FFF0F0".toColorInt())
        }

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    private fun formatDisplayDate(isoDate: String): String {
        return try {
            val parts = isoDate.split("T", " ")
            val datePart = parts[0].split("-")
            if (datePart.size == 3) {
                "${datePart[2]}/${datePart[1]}/${datePart[0]}"
            } else {
                isoDate
            }
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun formatTime(isoDate: String): String {
        return try {
            val parts = isoDate.split("T", " ")
            if (parts.size > 1) {
                val timePart = parts[1].split(":")
                if (timePart.size >= 2) {
                    "${timePart[0]}:${timePart[1]}"
                } else {
                    "00:00"
                }
            } else {
                "00:00"
            }
        } catch (e: Exception) {
            "00:00"
        }
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val layoutDateHeader: LinearLayout = itemView.findViewById(R.id.layoutDateHeader)
        val tvDateHeader: TextView = itemView.findViewById(R.id.tvDateHeader)
        val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvNote: TextView = itemView.findViewById(R.id.tvNote)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }
}
