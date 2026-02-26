package com.example.expensetracker.features.home

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.core.base.BaseActivity
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.models.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.features.home.HomeController.HomeListener

/**
 * HomeActivity — View cho màn hình Trang chủ / Dashboard.
 */
class HomeActivity : BaseActivity(R.layout.activity_home), HomeListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: HomeAdapter
    private lateinit var controller: HomeController

    override fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar  = findViewById(R.id.progressBar)
        tvEmpty      = findViewById(R.id.tvEmpty)

        adapter = HomeAdapter { expense ->
            // TODO: mở màn hình chi tiết chi tiêu
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val db         = AppDatabase.getInstance(this)
        val repository = ExpenseRepository(db)
        controller     = HomeController(repository, this)
    }

    override fun initObservers() {
        controller.loadExpenses()
    }

    // ─── HomeListener ────────────────────────────────────────────────────────

    override fun onExpensesLoaded(expenses: List<Expense>) {
        adapter.updateData(expenses)
        tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
