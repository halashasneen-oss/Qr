package com.halashasneen.nova.ui.vault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.halashasneen.nova.data.model.VaultItem
import com.halashasneen.nova.databinding.ItemVaultBinding

class VaultAdapter(
    private val onClick: (VaultItem) -> Unit,
    private val onDelete: (VaultItem) -> Unit
) : ListAdapter<VaultItem, VaultAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(val binding: ItemVaultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVaultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvVaultTitle.text = item.title
        holder.binding.tvVaultContent.text = item.content
        holder.binding.root.setOnClickListener { onClick(item) }
        holder.binding.ivDeleteVault.setOnClickListener { onDelete(item) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VaultItem>() {
            override fun areItemsTheSame(oldItem: VaultItem, newItem: VaultItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: VaultItem, newItem: VaultItem) =
                oldItem == newItem
        }
    }
}
