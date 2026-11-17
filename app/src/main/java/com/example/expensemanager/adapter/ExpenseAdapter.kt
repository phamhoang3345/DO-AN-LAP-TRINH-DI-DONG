package com.example.expensemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.databinding.ItemExpenseBinding
import com.example.expensemanager.model.Expense

class ExpenseAdapter(private var expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

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
        val amountDisplay = if (expense.amount.endsWith("đ")) expense.amount else "${expense.amount} đ"
        holder.binding.tvAmount.text = amountDisplay

        val categoryStr = expense.category ?: "Khác"
        holder.binding.tvCategoryDate.text = "$categoryStr • ${expense.date}"
    }

    override fun getItemCount(): Int = expenseList.size

    fun updateData(newList: List<Expense>) {
        expenseList = newList
        notifyDataSetChanged()
    }
}
