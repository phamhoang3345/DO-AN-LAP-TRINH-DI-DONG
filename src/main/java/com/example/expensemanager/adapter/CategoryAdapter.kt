package com.example.expensemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.data.local.entity.CategoryEntity
import com.example.expensemanager.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categoryList: List<CategoryEntity>,
    var onItemClick: ((CategoryEntity) -> Unit)? = null,
    var onDeleteClick: ((CategoryEntity) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.tvCategoryName.text = category.name
        holder.binding.tvCategoryIcon.text = category.icon ?: "🏷️"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(category)
        }

        holder.binding.btnDeleteCategory.setOnClickListener {
            onDeleteClick?.invoke(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateData(newList: List<CategoryEntity>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}
