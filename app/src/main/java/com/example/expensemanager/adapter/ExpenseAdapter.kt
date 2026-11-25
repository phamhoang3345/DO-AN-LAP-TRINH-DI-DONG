package com.example.expensemanager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.common.ImageLoader
import com.example.expensemanager.databinding.ItemExpenseBinding
import com.example.expensemanager.model.Expense

class ExpenseAdapter(
    initialList: List<Expense> = emptyList(),
    var onItemClick: ((Expense) -> Unit)? = null
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    init {
        if (initialList.isNotEmpty()) {
            submitList(initialList)
        }
    }

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                @Suppress("DEPRECATION")
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(pos))
                }
            }
        }

        fun bind(expense: Expense) {
            binding.tvTitle.text = expense.title

            val rawAmount = expense.amount.trim()
            val cleanAmount = if (rawAmount.endsWith("đ")) rawAmount else "$rawAmount đ"

            val isIncome = expense.type == "INCOME" ||
                expense.title.startsWith("Thu nhập") ||
                (expense.category?.contains("Thu nhập") == true)

            if (isIncome) {
                binding.tvAmount.text = if (cleanAmount.startsWith("+")) {
                    cleanAmount
                } else {
                    "+ $cleanAmount"
                }
                binding.tvAmount.setTextColor(INCOME_COLOR)
            } else {
                binding.tvAmount.text = if (cleanAmount.startsWith("-")) {
                    cleanAmount
                } else {
                    "- $cleanAmount"
                }
                binding.tvAmount.setTextColor(EXPENSE_COLOR)
            }

            val categoryStr = expense.category?.trim().orEmpty()
            binding.tvCategoryDate.text = if (categoryStr.isNotEmpty()) {
                "$categoryStr • ${expense.date}"
            } else {
                expense.date
            }

            // Tải ảnh minh họa giao dịch qua Glide với Cache tối ưu
            ImageLoader.loadTransactionImage(binding.ivIcon, url = null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ExpenseViewHolder) {
        super.onViewRecycled(holder)
        // Giải phóng Glide ImageView khi item cuộn khỏi màn hình để giải phóng RAM
        ImageLoader.clear(holder.binding.ivIcon)
    }

    // Giữ tên hàm updateData để tương thích với code đang gọi ở các Fragment/Activity khác
    fun updateData(newList: List<Expense>) {
        submitList(newList.toList())
    }

    private class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return if (oldItem.id != null && newItem.id != null) {
                oldItem.id == newItem.id
            } else {
                oldItem === newItem
            }
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }

    private companion object {
        private val INCOME_COLOR = Color.parseColor("#388E3C")
        private val EXPENSE_COLOR = Color.parseColor("#C66A42")
    }
}
