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
import com.bumptech.glide.Glide

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

        fun bind() { }
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
            // 1. Glide를 사용하여 imageUrl의 이미지를 clothingImage 뷰에 로드
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(binding.clothingImage)

            // 2. 나머지 선택모드 관련 UI 업데이트 로직은 그대로 유지
            val isSelected = selectedItemUuids.contains(item.uuid)
            itemView.isActivated = isSelected
            binding.clothingOverlay.isActivated = isSelected
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
