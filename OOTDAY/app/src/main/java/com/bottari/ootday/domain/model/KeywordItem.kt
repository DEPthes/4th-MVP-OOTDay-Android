package com.bottari.ootday.domain.model

sealed class KeywordItem {
    object AddButton : KeywordItem()

    data class KeywordData(
        val name: String,
        val isSelected: Boolean = false, // ✨ 선택 상태를 저장하는 프로퍼티
    ) : KeywordItem()
}
