package com.example.expensetracker.features.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.features.wallet.WalletModel
import com.example.expensetracker.features.wallet.WalletType
import java.text.DecimalFormat

/**
 * WalletSelectionAdapter — Adapter dành riêng cho việc chọn ví trong BottomSheet.
 */
class WalletSelectionAdapter(
    private val wallets: List<WalletModel>,
    private val selectedWalletId: Int,
    private val onWalletSelected: (WalletModel) -> Unit
) : RecyclerView.Adapter<WalletSelectionAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallet = wallets[position]
        
        holder.tvName.text = wallet.name
        holder.tvBalance.text = "${decimalFormat.format(wallet.balance)} ${wallet.currency}"
        
        // Cài đặt icon dựa trên loại ví
        val iconRes = when (wallet.type) {
            WalletType.CASH -> R.drawable.ic_boxed_cash
            WalletType.BANK_ACCOUNT -> R.drawable.ic_boxed_bank
            WalletType.E_WALLET -> R.drawable.ic_boxed_ewallet
        }
        holder.ivIcon.setImageResource(iconRes)
        
        // Xử lý trạng thái chọn
        val isSelected = wallet.id == selectedWalletId
        holder.itemView.isSelected = isSelected
        holder.ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onWalletSelected(wallet)
        }
    }

    override fun getItemCount(): Int = wallets.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivWalletIcon)
        val tvName: TextView = view.findViewById(R.id.tvWalletName)
        val tvBalance: TextView = view.findViewById(R.id.tvWalletBalance)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheck)
    }
}
