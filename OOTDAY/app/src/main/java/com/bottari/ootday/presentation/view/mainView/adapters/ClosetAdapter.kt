// ClosetAdapter.kt
package com.bottari.ootday.presentation.view.mainView.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bottari.ootday.databinding.ClosetImageItemBinding
import com.bottari.ootday.databinding.ClosetPlusItemBinding
import com.bottari.ootday.domain.model.DisplayableClosetItem

class ClosetAdapter(
    private val onItemClick: (DisplayableClosetItem) -> Unit,
) : ListAdapter<DisplayableClosetItem, RecyclerView.ViewHolder>(ClosetItemDiffCallback()) {
    private var isSelectionMode = false
    private var selectedItemUuids: Set<String> = emptySet()

    fun setSelectionMode(enabled: Boolean) {
        if (isSelectionMode != enabled) {
            isSelectionMode = enabled
            notifyDataSetChanged()
        }
    }

    fun setSelectedItems(itemUuids: Set<String>) {
        if (selectedItemUuids != itemUuids) {
            selectedItemUuids = itemUuids
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val VIEW_TYPE_PLUS = 0
        private const val VIEW_TYPE_IMAGE = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DisplayableClosetItem.AddButton -> VIEW_TYPE_PLUS
            is DisplayableClosetItem.ClosetData -> VIEW_TYPE_IMAGE
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_PLUS -> {
                val binding =
                    ClosetPlusItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                PlusViewHolder(binding, onItemClick)
            }
            VIEW_TYPE_IMAGE -> {
                val binding =
                    ClosetImageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                ImageViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        when (holder) {
            is PlusViewHolder -> holder.bind()
            is ImageViewHolder -> (item as? DisplayableClosetItem.ClosetData)?.let { holder.bind(it) }
        }
    }

    inner class PlusViewHolder(
        private val binding: ClosetPlusItemBinding,
        private val onItemClick: (DisplayableClosetItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.plusButton.setOnClickListener {
                onItemClick(DisplayableClosetItem.AddButton)
            }
        }

        fun bind() { /* 바인딩할 데이터가 없음 */ }
    }

    inner class ImageViewHolder(
        private val binding: ClosetImageItemBinding,
        private val onItemClick: (DisplayableClosetItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.clothingImage.setOnClickListener {
                val item = getItem(adapterPosition)
                if (item is DisplayableClosetItem.ClosetData) {
                    onItemClick(item)
                }
            }
        }

        fun bind(item: DisplayableClosetItem.ClosetData) {
            val isSelected = selectedItemUuids.contains(item.uuid)

            // ✅ itemView와 overlay의 activated 상태를 일치시킵니다.
            itemView.isActivated = isSelected
            binding.clothingOverlay.isActivated = isSelected

            // ✅ Checkbox의 activated 상태를 일치시키고, visible 상태를 제어합니다.
            binding.checkbox.isActivated = isSelected
            binding.checkbox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        }
    }

    class ClosetItemDiffCallback : DiffUtil.ItemCallback<DisplayableClosetItem>() {
        override fun areItemsTheSame(
            oldItem: DisplayableClosetItem,
            newItem: DisplayableClosetItem,
        ): Boolean =
            when {
                oldItem is DisplayableClosetItem.AddButton && newItem is DisplayableClosetItem.AddButton -> true
                oldItem is DisplayableClosetItem.ClosetData && newItem is DisplayableClosetItem.ClosetData -> oldItem.uuid == newItem.uuid
                else -> false
            }

        override fun areContentsTheSame(
            oldItem: DisplayableClosetItem,
            newItem: DisplayableClosetItem,
        ): Boolean = oldItem == newItem
    }
}
