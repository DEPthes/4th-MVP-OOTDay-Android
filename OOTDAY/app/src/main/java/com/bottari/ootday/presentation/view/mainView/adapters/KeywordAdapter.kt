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
    private val onItemClick: (KeywordItem) -> Unit
) : ListAdapter<KeywordItem, RecyclerView.ViewHolder>(KeywordItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_PLUS = 0
        private const val VIEW_TYPE_KEYWORD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is KeywordItem.AddButton -> VIEW_TYPE_PLUS
            is KeywordItem.KeywordData -> VIEW_TYPE_KEYWORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLUS -> {
                val binding = ClosetMpPlusItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PlusViewHolder(binding, onItemClick)
            }
            VIEW_TYPE_KEYWORD -> {
                val binding = ClosetMpItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                KeywordViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PlusViewHolder -> holder.bind()
            is KeywordViewHolder -> (item as? KeywordItem.KeywordData)?.let { holder.bind(it) }
        }
    }

    class PlusViewHolder(
        private val binding: ClosetMpPlusItemBinding,
        private val onItemClick: (KeywordItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(KeywordItem.AddButton)
            }
        }
        fun bind() { /* No data to bind */ }
    }

    inner class KeywordViewHolder(
        private val binding: ClosetMpItemBinding,
        private val onItemClick: (KeywordItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                // adapterPosition이 유효할 때만 클릭 처리
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(item: KeywordItem.KeywordData) {
            binding.moodButtonText.text = item.name
            // ✨ 이 한 줄이 핵심입니다. isSelected 값에 따라 셀렉터가 자동으로 스타일을 바꿉니다.
            itemView.isSelected = item.isSelected
        }
    }

    class KeywordItemDiffCallback : DiffUtil.ItemCallback<KeywordItem>() {
        override fun areItemsTheSame(oldItem: KeywordItem, newItem: KeywordItem): Boolean {
            return oldItem is KeywordItem.KeywordData && newItem is KeywordItem.KeywordData && oldItem.name == newItem.name ||
                    oldItem is KeywordItem.AddButton && newItem is KeywordItem.AddButton
        }

        override fun areContentsTheSame(oldItem: KeywordItem, newItem: KeywordItem): Boolean {
            return oldItem == newItem
        }
    }
}