package com.bottari.ootday.data.repository

import com.bottari.ootday.domain.model.ClosetItem
import kotlinx.coroutines.delay
import okhttp3.MultipartBody

class ClosetRepository {
    // ✨ 코디 조합 결과를 반환하는 가짜 API 호출 함수 추가
    suspend fun getCombinationResult(): List<String> {
        // 가짜 네트워크 딜레이
        delay(1500)

        // ✨ 실제로는 서버에서 이미지 URL 리스트를 받아옵니다.
        // 테스트를 위해 4개의 이미지 URL만 반환합니다.
        return listOf(
            "https://picsum.photos/id/10/400/600",
            "https://picsum.photos/id/20/400/600",
            "https://picsum.photos/id/30/400/600",
            "https://picsum.photos/id/40/400/600",
        )
    }

    fun getClosetItems(category: String): List<ClosetItem> {
        // TODO: 실제 API 호출 로직으로 교체
        return when (category) {
            "상의" ->
                listOf(
                    ClosetItem(1, "uuid-1", "캐주얼 상의", "상의", "캐주얼", "예시 상의입니다."),
                    ClosetItem(2, "uuid-2", "포멀 상의", "상의", "포멀", "예시 상의입니다."),
                    ClosetItem(3, "uuid-3", "ㅃ 상의", "상의", "포멀", "예시 상의입니다."),
                    ClosetItem(4, "uuid-4", "포멀 상의", "상의", "포멀", "예시 상의입니다."),
                )
            "하의" ->
                listOf(
                    ClosetItem(5, "uuid-5", "데님 팬츠", "하의", "스트릿", "예시 하의입니다."),
                )
            "신발" ->
                listOf(
                    ClosetItem(6, "uuid-6", "운동화", "신발", "스포티", "예시 신발입니다."),
                )
            else -> emptyList()
        }
    }

    suspend fun uploadFile(file: MultipartBody.Part): Boolean {
        delay(1000)
        return true
    }
}
