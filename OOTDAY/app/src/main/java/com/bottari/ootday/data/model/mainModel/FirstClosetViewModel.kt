package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.domain.model.ClosetItem
import com.bottari.ootday.domain.model.DisplayableClosetItem
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class FirstClosetViewModel(private val repository: ClosetRepository) : ViewModel() {

    private val _closetItems = MutableLiveData<List<DisplayableClosetItem>>()
    val closetItems: LiveData<List<DisplayableClosetItem>> = _closetItems

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
        currentCategory = category
        viewModelScope.launch {
            val items = repository.getClosetItems(category).map { dto ->
                val isSelected = _selectedItems.value?.any { selected ->
                    selected.uuid == dto.uuid
                } ?: false
                DisplayableClosetItem.ClosetData(dto.id, dto.uuid, dto.name, dto.category, dto.mood, dto.description, isSelected)
            }
            val listWithPlusItem = listOf(DisplayableClosetItem.AddButton) + items
            _closetItems.value = listWithPlusItem

            _isAllSelectedInCurrentCategory.value = false
            updateAllSelectedState()
        }
    }

    fun postClosetItem(imagePart: MultipartBody.Part) {
        viewModelScope.launch {
            val success = repository.uploadFile(imagePart)
            if (success) {
                loadItemsByCategory(currentCategory)
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
        val allItemsInCurrentCategory = _closetItems.value?.filterIsInstance<DisplayableClosetItem.ClosetData>()?.map { it.uuid }.orEmpty()
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
                val itemsToAdd = allItemsInCurrentCategory.filter { item ->
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

    fun onCategorySelected(category: String, isSelected: Boolean) {
        if (isSelected) {
            // 이 로직은 updateCategorySelection에서만 사용되므로 ViewModel에서는 필요하지 않습니다.
        } else {
            // 이 로직도 마찬가지로 필요하지 않습니다.
        }
    }
}