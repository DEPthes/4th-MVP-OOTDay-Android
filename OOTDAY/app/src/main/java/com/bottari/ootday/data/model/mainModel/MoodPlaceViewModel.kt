package com.bottari.ootday.data.model.mainModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.StylingRepository
import com.bottari.ootday.data.service.MoodDto
import com.bottari.ootday.data.service.PlaceDto
import com.bottari.ootday.domain.model.ClothingItemDto
import com.bottari.ootday.domain.model.DisplayableClosetItem
import com.bottari.ootday.domain.model.KeywordItem
import com.bottari.ootday.domain.model.StylingRequest
import kotlinx.coroutines.launch

// '코디하기' 흐름 전체를 관리하는 공유 뷰모델
class MoodPlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StylingRepository(application.applicationContext)

    // --- 1. 데이터 저장소 (장바구니 역할) ---
    // FirstClosetFragment에서 선택한 옷 아이템들
    private val _selectedClothes = MutableLiveData<List<DisplayableClosetItem.ClosetData>>()
    val selectedClothes: LiveData<List<DisplayableClosetItem.ClosetData>> get() = _selectedClothes

    // MoodFragment에서 선택한 무드 키워드들
    private val _selectedMoods = MutableLiveData<List<KeywordItem.KeywordData>>(emptyList())
    val selectedMoods: LiveData<List<KeywordItem.KeywordData>> get() = _selectedMoods

    // PlaceFragment에서 선택한 장소 키워드 (장소는 1개만 선택)
    private val _selectedPlace = MutableLiveData<KeywordItem.KeywordData?>()
    val selectedPlace: LiveData<KeywordItem.KeywordData?> get() = _selectedPlace

    // --- 최종 결과를 위한 데이터 저장소 ---
    private val _stylingResultUrls = MutableLiveData<List<String>>()
    val stylingResultUrls: LiveData<List<String>> get() = _stylingResultUrls

    private val _isLoadingResult = MutableLiveData<Boolean>()
    val isLoadingResult: LiveData<Boolean> get() = _isLoadingResult


    // --- 2. UI 상태 관리 ---
    // 현재 화면(무드/장소)에 표시될 키워드 목록
    private val _keywords = MutableLiveData<List<KeywordItem>>()
    val keywords: LiveData<List<KeywordItem>> = _keywords

    // 선택된 키워드 개수를 표시할 텍스트 (예: "(2/3)")
    private val _selectedCountText = MutableLiveData<String>()
    val selectedCountText: LiveData<String> = _selectedCountText

    // '완료' 또는 '다음' 버튼의 활성화 상태
    private val _isFinishButtonEnabled = MutableLiveData<Boolean>()
    val isFinishButtonEnabled: LiveData<Boolean> = _isFinishButtonEnabled


    // --- 3. 내부 로직 관리 ---
    private val defaultMoods = listOf("미니멀", "캐주얼", "스트릿", "아메카지")
    private val defaultPlaces = listOf("카페", "전시회", "공원 산책", "헬스장", "루프탑 바", "등산")
    private val userAddedKeywords = mutableMapOf<String, MutableList<String>>("mood" to mutableListOf(), "place" to mutableListOf())
    private var currentKeywordType = "mood" // 현재 화면이 '무드'인지 '장소'인지 구분


    // --- 4. 공개 함수 ---

    // FirstClosetFragment에서 '스타일링 시작' 버튼 누를 때 호출
    fun setSelectedClothes(clothes: List<DisplayableClosetItem.ClosetData>) {
        _selectedClothes.value = clothes
        Log.d("StylingDebug", "✅ [옷] 장바구니에 담김: ${clothes.size}개")
    }

    // MoodFragment가 생성될 때 호출
    fun loadMoodKeywords() {
        currentKeywordType = "mood"
        viewModelScope.launch {
            repository.getMoodKeywords()
                .onSuccess { serverKeywords ->
                    val serverKeywordNames = serverKeywords.map { it.moodName }
                    val combinedList = (defaultMoods + serverKeywordNames + userAddedKeywords["mood"]!!).distinct()
                    updateUi(combinedList, 3)
                }
                .onFailure { updateUi(defaultMoods + userAddedKeywords["mood"]!!, 3) }
        }
    }

    // PlaceFragment가 생성될 때 호출
    fun loadPlaceKeywords() {
        currentKeywordType = "place"
        viewModelScope.launch {
            repository.getPlaceKeywords()
                .onSuccess { serverKeywords ->
                    val serverKeywordNames = serverKeywords.map { it.placeName }
                    val combinedList = (defaultPlaces + serverKeywordNames + userAddedKeywords["place"]!!).distinct()
                    updateUi(combinedList, 1)
                }
                .onFailure { updateUi(defaultPlaces + userAddedKeywords["place"]!!, 1) }
        }
    }

    // '+' 버튼으로 새 키워드 추가 시 (POST)
    fun addNewKeyword(newKeyword: String) {
        viewModelScope.launch {
            if (currentKeywordType == "mood") {
                if ((defaultMoods + userAddedKeywords["mood"]!!).contains(newKeyword)) return@launch
                repository.addMoodKeyword(newKeyword).onSuccess {
                    userAddedKeywords["mood"]?.add(newKeyword)
                    loadMoodKeywords()
                }
            } else {
                if ((defaultPlaces + userAddedKeywords["place"]!!).contains(newKeyword)) return@launch
                repository.addPlaceKeyword(newKeyword).onSuccess {
                    userAddedKeywords["place"]?.add(newKeyword)
                    loadPlaceKeywords()
                }
            }
        }
    }

    // 키워드 클릭 시 선택/해제 처리
    fun onKeywordClicked(clickedKeyword: KeywordItem.KeywordData) {
        if (currentKeywordType == "mood") {
            // 무드 선택 로직 (최대 3개)
            val currentSelected = _selectedMoods.value.orEmpty().toMutableList()
            val isAlreadySelected = currentSelected.any { it.name == clickedKeyword.name }

            if (isAlreadySelected) {
                currentSelected.removeIf { it.name == clickedKeyword.name }
            } else {
                if (currentSelected.size < 3) {
                    currentSelected.add(clickedKeyword)
                }
            }
            _selectedMoods.value = currentSelected
        } else {
            // 장소 선택 로직 (최대 1개)
            val currentSelected = _selectedPlace.value
            _selectedPlace.value = if (currentSelected?.name == clickedKeyword.name) null else clickedKeyword
        }
        updateUiWithCurrentKeywords()
    }

    // --- 5. 내부 UI 업데이트 함수 ---

    private fun updateUi(keywordNames: List<String>, maxCount: Int) {
        val selectedNames = if (currentKeywordType == "mood") {
            _selectedMoods.value.orEmpty().map { it.name }.toSet()
        } else {
            _selectedPlace.value?.let { setOf(it.name) } ?: emptySet()
        }

        val allItems = keywordNames.map { KeywordItem.KeywordData(it, selectedNames.contains(it)) }
        _keywords.value = listOf(KeywordItem.AddButton) + allItems
        _selectedCountText.value = "(${selectedNames.size}/$maxCount)"
        _isFinishButtonEnabled.value = selectedNames.isNotEmpty()
    }

    private fun updateUiWithCurrentKeywords() {
        val currentKeywordNames = _keywords.value.orEmpty().filterIsInstance<KeywordItem.KeywordData>().map { it.name }
        val maxCount = if (currentKeywordType == "mood") 3 else 1
        updateUi(currentKeywordNames, maxCount)
    }

    // ResultFragment에서 호출할 최종 함수
    fun requestStyling() {
        val clothes = _selectedClothes.value
        val moods = _selectedMoods.value
        val place = _selectedPlace.value
        Log.d("ClosetDebug", "requestStyling: 메서드 호출 ${moods}, ${place}, ${clothes}")
        if (clothes.isNullOrEmpty() || moods.isNullOrEmpty() || place == null) return
        Log.d("ClosetDebug", "requestStyling: 메서드 if문 통과")

        _isLoadingResult.value = true
        viewModelScope.launch {
            val imageListDto = clothes.map {
                ClothingItemDto(
                    uuid = it.uuid,
                    name = it.name,
                    category = it.category,
                    mood = it.mood,
                    description = it.description,
                    imageUrl = it.imageUrl
                )
            }
            val moodListDto = moods.map { MoodDto(it.name) }
            val placeDto = PlaceDto(place.name)
            val request = StylingRequest(imageListDto, moodListDto, placeDto)

            repository.getStylingResult(request)
                .onSuccess { resultItems ->
                    // 👇 [수정] 서버에서 받은 결과 객체(ClothingItemDto)에서 imageUrl만 추출하여 LiveData에 저장
                    Log.e("StylingApi", "코디 결과 요청 성공")

                    _stylingResultUrls.value = resultItems.flatten().map { it.imageUrl }
                }
                .onFailure {
                    // 👇 [수정] 실패 시 에러 로그를 남기고, 결과 URL 목록을 비워 에러 상태임을 알림
                    Log.e("StylingApi", "코디 결과 요청 실패: ${it.message}")
                    _stylingResultUrls.value = emptyList() // 👈 실패 시 빈 리스트 전달
                }
            _isLoadingResult.value = false
        }
    }

}