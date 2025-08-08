package com.bottari.ootday.data.repository

import com.bottari.ootday.domain.model.ClosetItem

class ClosetRepository {
    fun getClosetItems(category: String): List<ClosetItem> {
        // TODO: 실제 API 호출 로직으로 교체
        return when (category) {
            "상의" -> listOf(
                ClosetItem(1, "uuid-1", "캐주얼 상의", "상의", "캐주얼", "예시 상의입니다."),
                ClosetItem(2, "uuid-2", "포멀 상의", "상의", "포멀", "예시 상의입니다."),
                ClosetItem(3, "uuid-3", "ㅃ 상의", "상의", "포멀", "예시 상의입니다."),
                ClosetItem(4, "uuid-4", "포멀 상의", "상의", "포멀", "예시 상의입니다.")
            )
            "하의" -> listOf(
                ClosetItem(5, "uuid-5", "데님 팬츠", "하의", "스트릿", "예시 하의입니다.")
            )
            "신발" -> listOf(
                ClosetItem(6, "uuid-6", "운동화", "신발", "스포티", "예시 신발입니다.")
            )
            else -> emptyList()
        }
    }
}