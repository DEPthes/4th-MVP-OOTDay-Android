package com.bottari.ootday.presentation.view.mainView.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bottari.ootday.databinding.ClosetImageItemBinding
import com.bottari.ootday.databinding.ClosetPlusItemBinding
import com.bottari.ootday.domain.model.DisplayableClosetItem

class ClosetAdapter(private val onItemClick: (DisplayableClosetItem) -> Unit) :
    ListAdapter<DisplayableClosetItem, RecyclerView.ViewHolder>(ClosetItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_PLUS = 0
        private const val VIEW_TYPE_IMAGE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayableClosetItem.AddButton -> VIEW_TYPE_PLUS
            is DisplayableClosetItem.ClosetData -> VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLUS -> {
                val binding = ClosetPlusItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlusViewHolder(binding, onItemClick)
            }
            VIEW_TYPE_IMAGE -> {
                val binding = ClosetImageItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ImageViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PlusViewHolder -> holder.bind()
            is ImageViewHolder -> (item as? DisplayableClosetItem.ClosetData)?.let { holder.bind(it) }
        }
    }

    class PlusViewHolder(
        private val binding: ClosetPlusItemBinding,
        private val onItemClick: (DisplayableClosetItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(DisplayableClosetItem.AddButton)
            }
        }
        fun bind() { /* No data to bind for Plus button */ }
    }

    class ImageViewHolder(
        private val binding: ClosetImageItemBinding,
        private val onItemClick: (DisplayableClosetItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisplayableClosetItem.ClosetData) {
            // TODO: 이미지 URL을 사용하여 이미지 로드
            // binding.clothingImage.load(item.imageUrl)

            // 선택 상태에 따른 오버레이 활성화
            binding.clothingOverlay.isActivated = item.isSelected

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class ClosetItemDiffCallback : DiffUtil.ItemCallback<DisplayableClosetItem>() {
        override fun areItemsTheSame(oldItem: DisplayableClosetItem, newItem: DisplayableClosetItem): Boolean {
            return when {
                oldItem is DisplayableClosetItem.AddButton && newItem is DisplayableClosetItem.AddButton -> true
                oldItem is DisplayableClosetItem.ClosetData && newItem is DisplayableClosetItem.ClosetData -> oldItem.uuid == newItem.uuid
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DisplayableClosetItem, newItem: DisplayableClosetItem): Boolean {
            return oldItem == newItem
        }
    }
}