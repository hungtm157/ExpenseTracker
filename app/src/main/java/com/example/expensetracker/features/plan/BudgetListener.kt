package com.example.expensetracker.features.plan

/**
 * BudgetListener — Interface định nghĩa các callback cho màn hình Kế hoạch ngân sách.
 */
interface BudgetListener {
    fun onBudgetsLoaded(budgets: List<BudgetModel>)
    fun onBudgetDetailLoaded(budget: BudgetModel)
    fun onBudgetCreated(budget: BudgetModel)
    fun onBudgetUpdated(budget: BudgetModel)
    fun onBudgetCompleted(budget: BudgetModel)
    fun onLoading(isLoading: Boolean)
    fun onError(message: String)
}
