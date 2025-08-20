package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ItemRepository
import com.bottari.ootday.domain.model.RecommendedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class SecondClosetViewModel(private val itemRepository: ItemRepository) : ViewModel() {

    private val _recommendationResult = MutableLiveData<RecommendedItem?>()
    val recommendationResult: LiveData<RecommendedItem?> = _recommendationResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    //추천 아이템을 API로 가져오는 함수
    fun fetchRecommendation(imagePart: MultipartBody.Part) {
        _isLoading.value = true
        viewModelScope.launch {
            itemRepository.findSimilarItem(imagePart)
                .onSuccess { result ->
                    _recommendationResult.value = result
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "알 수 없는 에러 발생"
                }
            _isLoading.value = false
        }
    }

    /**
     * ✨ 결과 화면으로 이동 후 LiveData를 초기화하여,
     * 화면 회전 등에서 다시 탐색되는 것을 방지합니다.
     */
    fun onRecommendationShown() {
        _recommendationResult.value = null
    }
}
