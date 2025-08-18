package com.bottari.ootday.domain.model

sealed class DisplayableClosetItem {
    data class ClosetData(
        val id: Long,
        val uuid: String,
        val name: String,
        val category: String,
        val mood: String,
        val description: String,
        val imageUrl: String, // 👈 이미지 URL 필드 추가
        var isSelected: Boolean = false,
    ) : DisplayableClosetItem()

    object AddButton : DisplayableClosetItem()
}
