package com.example.expensetracker.features.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import java.text.DecimalFormat
import android.graphics.Color
import android.content.res.ColorStateList

/**
 * WalletAdapter — Adapter cho RecyclerView hiển thị danh sách ví.
 */
class WalletAdapter(
    private var wallets: List<WalletModel>,
    private val onStatusChanged: (WalletModel, Boolean) -> Unit,
    private val onEditClick: (WalletModel) -> Unit,
    private val onDeleteClick: (WalletModel) -> Unit
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    private val decimalFormat = DecimalFormat("#,###")

    fun updateData(newWallets: List<WalletModel>) {
        this.wallets = newWallets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = wallets[position]
        holder.bind(wallet)
    }

    override fun getItemCount(): Int = wallets.size

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivWalletIcon: ImageView = itemView.findViewById(R.id.ivWalletIcon)
        private val tvWalletName: TextView = itemView.findViewById(R.id.tvWalletName)
        private val tvWalletType: TextView = itemView.findViewById(R.id.tvWalletType)
        private val tvWalletBalance: TextView = itemView.findViewById(R.id.tvWalletBalance)
        private val switchStatus: SwitchMaterial = itemView.findViewById(R.id.switchStatus)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(wallet: WalletModel) {
            tvWalletName.text = wallet.name
            tvWalletBalance.text = "${decimalFormat.format(wallet.balance)} đ"
            
            // Set icon based on type using the refined boxed icons
            when (wallet.type) {
                WalletType.CASH -> {
                    tvWalletType.text = "Tiền mặt"
                    ivWalletIcon.setImageResource(R.drawable.ic_boxed_cash)
                }
                WalletType.BANK_ACCOUNT -> {
                    tvWalletType.text = "Tài khoản ngân hàng"
                    ivWalletIcon.setImageResource(R.drawable.ic_boxed_bank)
                }
                WalletType.E_WALLET -> {
                    tvWalletType.text = "Ví điện tử"
                    ivWalletIcon.setImageResource(R.drawable.ic_boxed_ewallet)
                }
            }
            
            // Ensure no tint is applied to the boxed icon itself as it has its own colors
            ivWalletIcon.imageTintList = null

            switchStatus.isChecked = wallet.status == WalletStatus.ACTIVATE
            
            // Clear previous listener to avoid triggering on bind
            switchStatus.setOnCheckedChangeListener(null)
            switchStatus.isChecked = wallet.status == WalletStatus.ACTIVATE
            switchStatus.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged(wallet, isChecked)
            }

            btnEdit.setOnClickListener { onEditClick(wallet) }
            btnDelete.setOnClickListener { onDeleteClick(wallet) }
        }
    }
}
