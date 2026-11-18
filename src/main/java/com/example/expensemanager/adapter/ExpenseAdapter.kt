package com.example.expensemanager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.databinding.ItemExpenseBinding
import com.example.expensemanager.model.Expense

class ExpenseAdapter(
    private var expenseList: List<Expense>,
    var onItemClick: ((Expense) -> Unit)? = null
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.binding.tvTitle.text = expense.title
        val cleanAmount = if (expense.amount.endsWith("đ")) expense.amount else "${expense.amount} đ"

        val isIncome = expense.title.startsWith("Thu nhập") || expense.category?.contains("Thu nhập") == true
        if (isIncome) {
            val formattedIncomeAmount = if (cleanAmount.startsWith("+")) cleanAmount else "+ $cleanAmount"
            holder.binding.tvAmount.text = formattedIncomeAmount
            holder.binding.tvAmount.setTextColor(Color.parseColor("#388E3C"))
        } else {
            val formattedExpenseAmount = if (cleanAmount.startsWith("-")) cleanAmount else "- $cleanAmount"
            holder.binding.tvAmount.text = formattedExpenseAmount
            holder.binding.tvAmount.setTextColor(Color.parseColor("#C66A42"))
        }

        val categoryStr = expense.category ?: "Khác"
        holder.binding.tvCategoryDate.text = "$categoryStr • ${expense.date}"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(expense)
        }
    }

    override fun getItemCount(): Int = expenseList.size

    fun updateData(newList: List<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}
