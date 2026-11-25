package com.example.expensemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.databinding.ItemCategoryStatBinding
import com.example.expensemanager.model.CategoryStat
import java.util.Locale

class CategoryStatAdapter : ListAdapter<CategoryStat, CategoryStatAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryStatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemCategoryStatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryStat) {
            binding.tvCategoryIcon.text = item.icon
            binding.tvCategoryName.text = item.categoryName
            val percentText = String.format(Locale.getDefault(), "%.1f%%", item.percentage)
            binding.tvTransactionCount.text = "${item.transactionCount} giao dịch • $percentText"
            binding.tvCategoryAmount.text = item.formattedAmount
            binding.progressCategory.progress = item.percentage.toInt().coerceIn(0, 100)
            if (item.color != 0) {
                binding.progressCategory.setIndicatorColor(item.color)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryStat>() {
            override fun areItemsTheSame(oldItem: CategoryStat, newItem: CategoryStat): Boolean {
                return oldItem.categoryName == newItem.categoryName
            }

            override fun areContentsTheSame(oldItem: CategoryStat, newItem: CategoryStat): Boolean {
                return oldItem == newItem
            }
        }
    }
}
