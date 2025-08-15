package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.domain.model.RecommendedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class SecondClosetViewModel : ViewModel() {
    // 기존 코드: 서버로 보낼 이미지 파일 데이터
    private val _imagePart = MutableLiveData<MultipartBody.Part?>()
    val imagePart: LiveData<MultipartBody.Part?> = _imagePart

    fun setImageData(imagePart: MultipartBody.Part) {
        _imagePart.value = imagePart
    }

    // ✨ 새로 추가된 코드: 추천 결과 데이터를 위한 LiveData
    private val _recommendationResult = MutableLiveData<RecommendedItem?>()
    val recommendationResult: LiveData<RecommendedItem?> = _recommendationResult

    /**
     * ✨ Fake GET 요청: 가상으로 서버에서 추천 아이템을 가져옵니다.
     * 실제 Retrofit 구현 시 이 함수 내부만 교체하면 됩니다.
     */
    fun fetchRecommendation() {
        viewModelScope.launch {
            // 3초간 딜레이를 주어 로딩 화면을 보여줌
            delay(3000)

            // 가상 데이터 생성 (실제 Glide로 로드 가능한 이미지 URL 사용)
            val fakeItem =
                RecommendedItem(
                    imageUrl = "https://picsum.photos/seed/ootday/800/1200", // 테스트용 이미지 URL
                    productUrl = "https://www.musinsa.com/app/", // 테스트용 상품 링크
                )

            // LiveData에 결과 값 전달
            _recommendationResult.value = fakeItem
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
