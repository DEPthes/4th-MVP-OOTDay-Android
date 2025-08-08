package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.domain.model.ClosetItem
import com.bottari.ootday.domain.model.DisplayableClosetItem

class FirstClosetViewModel(
    private val repository: ClosetRepository
) : ViewModel() {

    private val _closetItems = MutableLiveData<List<DisplayableClosetItem>>()
    val closetItems: LiveData<List<DisplayableClosetItem>> get() = _closetItems

    private val _stylingButtonEnabled = MutableLiveData<Boolean>()
    val stylingButtonEnabled: LiveData<Boolean> get() = _stylingButtonEnabled

    private val _isTooltipVisible = MutableLiveData<Boolean>(true)
    val isTooltipVisible: LiveData<Boolean> get() = _isTooltipVisible

    private val _selectedCategories = MutableLiveData<Set<String>>(emptySet())
    val selectedCategories: LiveData<Set<String>> get() = _selectedCategories

    init {
        loadItemsByCategory("상의")
    }

    fun loadItemsByCategory(category: String) {
        val newItems = repository.getClosetItems(category)

        val displayableItems = newItems.map { dto ->
            DisplayableClosetItem.ClosetData(
                id = dto.id,
                uuid = dto.uuid,
                name = dto.name,
                category = dto.category,
                mood = dto.mood,
                description = dto.description
            )
        }

        val listWithPlusItem = listOf(DisplayableClosetItem.AddButton) + displayableItems
        _closetItems.value = listWithPlusItem
    }

    fun toggleItemSelection(item: DisplayableClosetItem.ClosetData) {
        val currentItems = _closetItems.value?.toMutableList() ?: return
        val index = currentItems.indexOfFirst { it is DisplayableClosetItem.ClosetData && it.id == item.id }
        if (index != -1) {
            val updatedItem = item.copy(isSelected = !item.isSelected)
            currentItems[index] = updatedItem
            _closetItems.value = currentItems

            // TODO: 선택된 카테고리 수 업데이트 로직
        }
    }

    fun onTooltipClicked() {
        _isTooltipVisible.value = false
    }

    fun onCategorySelected(category: String, isSelected: Boolean) {
        val currentCategories = _selectedCategories.value.orEmpty().toMutableSet()
        if (isSelected) {
            currentCategories.add(category)
        } else {
            currentCategories.remove(category)
        }
        _selectedCategories.value = currentCategories
        checkStylingButtonState()
    }

    private fun checkStylingButtonState() {
        val categories = _selectedCategories.value.orEmpty()
        val topSelected = categories.contains("상의")
        val bottomSelected = categories.contains("하의")
        val shoesSelected = categories.contains("신발")

        _stylingButtonEnabled.value = topSelected && bottomSelected && shoesSelected
    }
}