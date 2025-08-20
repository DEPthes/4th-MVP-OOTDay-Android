package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.domain.model.ClosetItem
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ClosetRepository) : ViewModel() {

    // 화면에 표시될 최신 옷 4개의 목록
    private val _recentClosetItems = MutableLiveData<List<ClosetItem>>()
    val recentClosetItems: LiveData<List<ClosetItem>> get() = _recentClosetItems

    // 옷장이 비었는지 여부를 알려주는 상태
    private val _isClosetEmpty = MutableLiveData<Boolean>()
    val isClosetEmpty: LiveData<Boolean> get() = _isClosetEmpty

    /**
     * 서버에서 전체 옷 목록을 가져와 최신 4개만 LiveData에 업데이트하는 함수
     */
    fun fetchRecentClosetItems() {
        viewModelScope.launch {
            repository.getMyCloset() // 기존 ClosetRepository의 함수를 재사용
                .onSuccess { allItems ->
                    if (allItems.isEmpty()) {
                        _isClosetEmpty.value = true
                    } else {
                        _isClosetEmpty.value = false
                        // 서버에서 받은 목록의 뒤쪽 아이템이 최신 아이템이라고 가정하고,
                        // 마지막 4개를 가져옵니다. (takeLast)
                        _recentClosetItems.value = allItems.takeLast(4)
                    }
                }
                .onFailure {
                    // 통신에 실패하면 일단 옷장이 빈 것으로 처리
                    _isClosetEmpty.value = true
                }
        }
    }
}