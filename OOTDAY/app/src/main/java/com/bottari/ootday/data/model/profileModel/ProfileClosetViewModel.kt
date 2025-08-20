package com.bottari.ootday.data.model.profileModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.domain.model.ClosetItem
import com.bottari.ootday.domain.model.DisplayableClosetItem
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileClosetViewModel(private val closetRepository: ClosetRepository) : ViewModel() {

    private var allClosetItems: List<ClosetItem> = emptyList()
    private val _closetItems = MutableLiveData<List<DisplayableClosetItem>>()
    val closetItems: LiveData<List<DisplayableClosetItem>> get() = _closetItems
    private var currentCategory: String = "상의"

    // --- 상태 관리를 위한 LiveData들 ---
    private val _selectedItems = MutableLiveData<List<DisplayableClosetItem.ClosetData>>(emptyList())
    val selectedItems: LiveData<List<DisplayableClosetItem.ClosetData>> get() = _selectedItems

    private val _isDeleteButtonEnabled = MutableLiveData<Boolean>(false)
    val isDeleteButtonEnabled: LiveData<Boolean> get() = _isDeleteButtonEnabled

    private val _isSelectionMode = MutableLiveData<Boolean>(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _isAllSelectedInCurrentCategory = MutableLiveData<Boolean>(false)
    val isAllSelectedInCurrentCategory: LiveData<Boolean> = _isAllSelectedInCurrentCategory

    init {
        loadAllClosetItems()
    }

    // --- 데이터 로딩 및 필터링 ---
    private fun loadAllClosetItems() {
        viewModelScope.launch {
            closetRepository.getMyCloset()
                .onSuccess { allItems ->
                    allClosetItems = allItems
                    showItemsForCategory(currentCategory)
                }
                .onFailure { _closetItems.value = listOf(DisplayableClosetItem.AddButton) }
        }
    }

    fun showItemsForCategory(category: String) {
        currentCategory = category
        val filteredList = allClosetItems.filter { it.category == category }
        val displayableItems = mutableListOf<DisplayableClosetItem>()
        displayableItems.add(DisplayableClosetItem.AddButton)
        displayableItems.addAll(filteredList.map {
            DisplayableClosetItem.ClosetData(it.id, it.uuid, it.name, it.category, it.mood, it.description, it.imageUrl, false)
        })
        _closetItems.value = displayableItems
        updateAllSelectedState()
    }

    // --- 아이템 추가 로직 ---
    fun uploadClothItems(context: Context, imageUris: List<Uri>) {
        viewModelScope.launch {
            for (uri in imageUris) {
                val fileStream = context.contentResolver.openInputStream(uri)
                val fileBytes = fileStream?.readBytes()
                fileStream?.close()
                if (fileBytes != null) {
                    val requestBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
                    closetRepository.createCloth(imagePart) // 카테고리 정보와 함께 업로드
                        .onFailure { Log.e("ClosetDebug", "Upload Failed: ${it.message}") }
                }
            }
            loadAllClosetItems()
        }
    }

    /**
     * [핵심 추가] 선택된 아이템들을 삭제하는 함수
     */
    fun deleteSelectedItems() {
        viewModelScope.launch {
            val itemsToDelete = _selectedItems.value.orEmpty()
            if (itemsToDelete.isEmpty()) return@launch

            // 선택된 아이템들을 하나씩 순회하며 삭제 API 호출
            for (item in itemsToDelete) {
                closetRepository.deleteClothItem(item.uuid)
                    .onFailure { Log.e("ClosetDebug", "Delete Failed for ${item.uuid}: ${it.message}") }
            }

            // 모든 삭제 작업 후, 선택 목록을 비우고 전체 목록을 새로고침
            _selectedItems.value = emptyList()
            loadAllClosetItems()
        }
    }

    // --- 선택 관련 로직 ---
    fun toggleItemSelection(item: DisplayableClosetItem.ClosetData) {
        val currentSelected = _selectedItems.value.orEmpty().toMutableList()
        if (currentSelected.any { it.uuid == item.uuid }) {
            currentSelected.removeIf { it.uuid == item.uuid }
        } else {
            currentSelected.add(item)
        }
        _selectedItems.value = currentSelected
        _isSelectionMode.value = currentSelected.isNotEmpty()
        _isDeleteButtonEnabled.value = currentSelected.isNotEmpty() // 삭제 버튼 상태 업데이트
        updateAllSelectedState()
    }

    fun selectAllItemsInCurrentCategory() {
        val allItemsInCurrentCategory = _closetItems.value?.filterIsInstance<DisplayableClosetItem.ClosetData>() ?: emptyList()
        val currentSelected = _selectedItems.value.orEmpty().toMutableList()

        if (_isAllSelectedInCurrentCategory.value == true) {
            // 모두 해제
            val currentCategoryUuids = allItemsInCurrentCategory.map { it.uuid }.toSet()
            currentSelected.removeIf { currentCategoryUuids.contains(it.uuid) }
        } else {
            // 모두 선택
            allItemsInCurrentCategory.forEach { item ->
                if (!currentSelected.any { it.uuid == item.uuid }) {
                    currentSelected.add(item)
                }
            }
        }
        _selectedItems.value = currentSelected
        _isSelectionMode.value = currentSelected.isNotEmpty()
        _isDeleteButtonEnabled.value = currentSelected.isNotEmpty()
        updateAllSelectedState()
    }

    private fun updateAllSelectedState() {
        val allUuids = _closetItems.value?.filterIsInstance<DisplayableClosetItem.ClosetData>()?.map { it.uuid }?.toSet() ?: emptySet()
        val selectedUuids = _selectedItems.value?.map { it.uuid }?.toSet() ?: emptySet()
        _isAllSelectedInCurrentCategory.value = allUuids.isNotEmpty() && allUuids.all { selectedUuids.contains(it) }
    }
}