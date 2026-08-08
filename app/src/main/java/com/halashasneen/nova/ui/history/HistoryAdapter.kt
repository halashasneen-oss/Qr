package com.halashasneen.nova.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.halashasneen.nova.R
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onClick: (QRHistoryItem) -> Unit,
    private val onFavoriteClick: (QRHistoryItem) -> Unit,
    private val onLongClick: (QRHistoryItem) -> Boolean,
    private val isSelected: (String) -> Boolean
) : ListAdapter<QRHistoryItem, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvTitle.text = item.displayTitle
        holder.binding.tvDate.text = dateFormat.format(Date(item.timestamp))
        holder.binding.tvTypeBadge.text = item.type.name

        holder.binding.ivFavorite.setImageResource(
            if (item.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        holder.binding.root.setBackgroundColor(
            if (isSelected(item.id))
                holder.itemView.context.getColor(R.color.brand_primary).let { (it and 0x00FFFFFF) or 0x22000000 }
            else android.graphics.Color.TRANSPARENT
        )

        holder.binding.root.setOnClickListener { onClick(item) }
        holder.binding.root.setOnLongClickListener { onLongClick(item) }
        holder.binding.ivFavorite.setOnClickListener { onFavoriteClick(item) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QRHistoryItem>() {
            override fun areItemsTheSame(oldItem: QRHistoryItem, newItem: QRHistoryItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: QRHistoryItem, newItem: QRHistoryItem) =
                oldItem == newItem
        }
    }
}
