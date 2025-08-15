package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bottari.ootday.domain.model.KeywordItem

class MoodPlaceViewModel : ViewModel() {
    private val _keywords = MutableLiveData<List<KeywordItem>>()
    val keywords: LiveData<List<KeywordItem>> = _keywords

    private val _selectedCountText = MutableLiveData<String>()
    val selectedCountText: LiveData<String> = _selectedCountText

    private val _isFinishButtonEnabled = MutableLiveData<Boolean>()
    val isFinishButtonEnabled: LiveData<Boolean> = _isFinishButtonEnabled

    // ✨ 현재 화면 타입에 맞는 기본 키워드 리스트
    private var currentBaseList = listOf<String>()

    // ✨ 사용자가 직접 추가한 키워드 리스트 (메모리)
    private val userAddedKeywords = mutableListOf<String>()

    // ✨ 현재 선택된 키워드 이름 Set
    private val selectedKeywordNames = mutableSetOf<String>()

    private var maxSelectionCount = 3

    private val staticMoods = listOf("우아한", "캐주얼", "빈티지", "로맨틱", "스타일링")
    private val staticPlaces = listOf("카페", "전시회", "공원 산책", "헬스장", "루프탑 바", "등산", "데이트", "도서관", "클럽", "해변")

    fun loadMoodKeywords() {
        maxSelectionCount = 3
        currentBaseList = staticMoods
        clearAndRefresh()
    }

    fun loadPlaceKeywords() {
        maxSelectionCount = 1
        currentBaseList = staticPlaces
        clearAndRefresh()
    }

    private fun clearAndRefresh() {
        userAddedKeywords.clear()
        selectedKeywordNames.clear()
        updateUi()
    }

    fun onKeywordClicked(clickedKeyword: KeywordItem.KeywordData) {
        val name = clickedKeyword.name

        if (maxSelectionCount == 1 && !selectedKeywordNames.contains(name)) {
            selectedKeywordNames.clear()
            selectedKeywordNames.add(name)
        } else {
            if (selectedKeywordNames.contains(name)) {
                selectedKeywordNames.remove(name)
            } else {
                if (selectedKeywordNames.size < maxSelectionCount) {
                    selectedKeywordNames.add(name)
                }
            }
        }
        updateUi()
    }

    // ✨ 누락되었던 새 키워드 추가 로직
    fun addNewKeyword(keywordText: String) {
        // 기본 리스트와 추가된 리스트에 중복이 없으면 추가
        if (!currentBaseList.contains(keywordText) && !userAddedKeywords.contains(keywordText)) {
            userAddedKeywords.add(keywordText)
            updateUi() // UI 갱신
        }
    }

    // ✨ UI 업데이트를 위한 단일 함수
    private fun updateUi() {
        val combinedList = currentBaseList + userAddedKeywords

        val updatedItems =
            combinedList.map { keywordName ->
                KeywordItem.KeywordData(
                    name = keywordName,
                    isSelected = selectedKeywordNames.contains(keywordName),
                )
            }

        _keywords.value = listOf(KeywordItem.AddButton) + updatedItems
        _selectedCountText.value = "(${selectedKeywordNames.size}/$maxSelectionCount)"
        _isFinishButtonEnabled.value = selectedKeywordNames.isNotEmpty()
    }
}
