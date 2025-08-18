package com.bottari.ootday.data.model.mainModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.domain.model.DisplayableClosetItem
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class FirstClosetViewModel(private val closetRepository: ClosetRepository) : ViewModel() {

    private val _closetItems = MutableLiveData<List<DisplayableClosetItem>>()
    val closetItems: LiveData<List<DisplayableClosetItem>> get() = _closetItems

    private val _isTooltipVisible = MutableLiveData<Boolean>(true)
    val isTooltipVisible: LiveData<Boolean> = _isTooltipVisible

    private val _selectedItems = MutableLiveData<List<DisplayableClosetItem.ClosetData>>(emptyList())
    val selectedItems: LiveData<List<DisplayableClosetItem.ClosetData>> = _selectedItems

    private val _isStylingButtonEnabled = MutableLiveData<Boolean>(false)
    val isStylingButtonEnabled: LiveData<Boolean> = _isStylingButtonEnabled

    private val _isSelectionMode = MutableLiveData<Boolean>(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _isAllSelectedInCurrentCategory = MutableLiveData<Boolean>(false)
    val isAllSelectedInCurrentCategory: LiveData<Boolean> = _isAllSelectedInCurrentCategory

    private var currentCategory: String = "상의"

    init {
        loadItemsByCategory("상의")
    }

    fun onTooltipClicked() {
        _isTooltipVisible.value = false
    }

    fun loadItemsByCategory(category: String) {
        Log.d("ClosetDebug", "ViewModel: loadItemsByCategory($category) 함수 실행됨") //debug
        currentCategory = category
        viewModelScope.launch {
            closetRepository.getMyCloset(category)
                .onSuccess { items ->
                    // API 응답(ClosetItem)을 UI 표시용(DisplayableClosetItem)으로 변환
                    val displayableItems = mutableListOf<DisplayableClosetItem>()
                    displayableItems.add(DisplayableClosetItem.AddButton) // '추가' 버튼 항상 표시
                    displayableItems.addAll(items.map {
                        DisplayableClosetItem.ClosetData(
                            id = it.id,
                            uuid = it.uuid,
                            name = it.name,
                            category = it.category,
                            mood = it.mood,
                            description = it.description,
                            // isSelected는 UI 상태이므로 여기서 false로 초기화
                            imageUrl = it.imageUrl,
                            isSelected = false
                        )
                    })
                    _closetItems.value = displayableItems
                }
                .onFailure {
                    // 에러 처리 (예: Toast 메시지 표시)
                    _closetItems.value = listOf(DisplayableClosetItem.AddButton) // 실패해도 '추가' 버튼은 보이게
                }
        }
    }

    fun uploadClothItem(imagePart: MultipartBody.Part) {
        viewModelScope.launch {
            closetRepository.uploadAndCreateCloth(imagePart, currentCategory)
                .onSuccess {
                    // 최종 성공 시 목록 새로고침
                    loadItemsByCategory(currentCategory)
                }
                .onFailure {
                    // 최종 실패 시 에러 처리
                }
        }
    }

    fun toggleItemSelection(item: DisplayableClosetItem.ClosetData) {
        val currentSelectedItems = _selectedItems.value.orEmpty().toMutableList()
        val isSelected = currentSelectedItems.any { it.uuid == item.uuid }

        if (isSelected) {
            currentSelectedItems.removeIf { it.uuid == item.uuid }
        } else {
            currentSelectedItems.add(item.copy(isSelected = true))
        }

        _selectedItems.value = currentSelectedItems
        _isSelectionMode.value = currentSelectedItems.isNotEmpty()
        updateButtonEnabledState()
        updateAllSelectedState()
    }

    private fun updateAllSelectedState() {
        val allItemsInCurrentCategory =
            _closetItems.value
                ?.filterIsInstance<DisplayableClosetItem.ClosetData>()
                ?.map { it.uuid }
                .orEmpty()
        val selectedItemsInCurrentCategory = _selectedItems.value?.filter { allItemsInCurrentCategory.contains(it.uuid) }.orEmpty()

        _isAllSelectedInCurrentCategory.value = allItemsInCurrentCategory.isNotEmpty() &&
            allItemsInCurrentCategory.size == selectedItemsInCurrentCategory.size
    }

    private fun updateButtonEnabledState() {
        val selectedTop = _selectedItems.value?.any { it.category == "상의" } ?: false
        val selectedBottom = _selectedItems.value?.any { it.category == "하의" } ?: false
        val selectedShoes = _selectedItems.value?.any { it.category == "신발" } ?: false
        _isStylingButtonEnabled.value = selectedTop && selectedBottom && selectedShoes
    }

    fun selectAllItemsInCurrentCategory() {
        viewModelScope.launch {
            val allItemsInCurrentCategory = _closetItems.value?.filterIsInstance<DisplayableClosetItem.ClosetData>() ?: emptyList()
            val currentSelectedItems = _selectedItems.value.orEmpty().toMutableList()
            val currentCategoryUuids = allItemsInCurrentCategory.map { it.uuid }.toSet()

            // ⭐ 모두 선택 상태를 확인하여 토글
            if (_isAllSelectedInCurrentCategory.value == true) {
                // 이미 모두 선택된 상태이므로 모두 해제합니다.
                currentSelectedItems.removeIf { currentCategoryUuids.contains(it.uuid) }
            } else {
                // 모두 선택된 상태가 아니므로 모두 선택합니다.
                val itemsToAdd =
                    allItemsInCurrentCategory
                        .filter { item ->
                            !currentSelectedItems.any { it.uuid == item.uuid }
                        }.map { it.copy(isSelected = true) }
                currentSelectedItems.addAll(itemsToAdd)
            }

            _selectedItems.value = currentSelectedItems
            _isSelectionMode.value = currentSelectedItems.isNotEmpty()
            updateButtonEnabledState()
            updateAllSelectedState() // ⭐ 모두 선택 상태 업데이트
        }
    }

    fun onCategorySelected(
        category: String,
        isSelected: Boolean,
    ) {
        if (isSelected) {
            // 이 로직은 updateCategorySelection에서만 사용되므로 ViewModel에서는 필요하지 않습니다.
        } else {
            // 이 로직도 마찬가지로 필요하지 않습니다.
        }
    }
}
