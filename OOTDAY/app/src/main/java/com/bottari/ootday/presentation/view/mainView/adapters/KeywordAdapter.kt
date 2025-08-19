package com.bottari.ootday.presentation.view.mainView.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bottari.ootday.databinding.ClosetMpItemBinding
import com.bottari.ootday.databinding.ClosetMpPlusItemBinding
import com.bottari.ootday.domain.model.KeywordItem

class KeywordAdapter(
    private val onItemClick: (KeywordItem) -> Unit,
) : ListAdapter<KeywordItem, RecyclerView.ViewHolder>(KeywordItemDiffCallback()) {
    companion object {
        private const val VIEW_TYPE_PLUS = 0
        private const val VIEW_TYPE_KEYWORD = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is KeywordItem.AddButton -> VIEW_TYPE_PLUS
            is KeywordItem.KeywordData -> VIEW_TYPE_KEYWORD
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_PLUS -> {
                val binding =
                    ClosetMpPlusItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                PlusViewHolder(binding, onItemClick)
            }
            VIEW_TYPE_KEYWORD -> {
                val binding =
                    ClosetMpItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                KeywordViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        when (holder) {
            is PlusViewHolder -> holder.bind(item as KeywordItem.AddButton)
            is KeywordViewHolder -> holder.bind(item as KeywordItem.KeywordData)
        }
    }

    class PlusViewHolder(
        private val binding: ClosetMpPlusItemBinding,
        private val onItemClick: (KeywordItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        // bind 함수에서 리스너 설정
        fun bind(item: KeywordItem.AddButton) {
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    inner class KeywordViewHolder(
        private val binding: ClosetMpItemBinding,
        private val onItemClick: (KeywordItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        // bind 함수에서 리스너 설정
        fun bind(item: KeywordItem.KeywordData) {
            binding.moodButtonText.text = item.name
            itemView.isSelected = item.isSelected

            // 클릭 리스너를 여기서 설정하여 항상 올바른 아이템을 참조
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class KeywordItemDiffCallback : DiffUtil.ItemCallback<KeywordItem>() {
        override fun areItemsTheSame(
            oldItem: KeywordItem,
            newItem: KeywordItem,
        ): Boolean =
            oldItem is KeywordItem.KeywordData &&
                newItem is KeywordItem.KeywordData &&
                oldItem.name == newItem.name ||
                oldItem is KeywordItem.AddButton &&
                newItem is KeywordItem.AddButton

        override fun areContentsTheSame(
            oldItem: KeywordItem,
            newItem: KeywordItem,
        ): Boolean = oldItem == newItem
    }
}
