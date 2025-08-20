package com.bottari.ootday.data.model.mainModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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

class FirstClosetViewModel(private val closetRepository: ClosetRepository) : ViewModel() {

    // 👇 서버에서 받은 '전체' 옷 목록을 저장(캐싱)하는 변수
    private var allClosetItems: List<ClosetItem> = emptyList()

    // 화면에 보여줄 아이템 목록 (필터링된 결과)
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
        loadAllClosetItems()
    }

    fun onTooltipClicked() {
        _isTooltipVisible.value = false
    }

    // [핵심] 서버에서 '전체' 옷 목록을 가져와 캐시에 저장하고, 현재 카테고리를 표시하는 함수
    private fun loadAllClosetItems() {
        viewModelScope.launch {
            closetRepository.getMyCloset()
                .onSuccess { allItems ->
                    allClosetItems = allItems // 전체 목록을 캐시에 저장
                    showItemsForCategory(currentCategory) // 저장 후 현재 카테고리를 화면에 표시
                }
                .onFailure {
                    // 통신 실패 시에는 '추가' 버튼만 보이도록 처리
                    _closetItems.value = listOf(DisplayableClosetItem.AddButton)
                }
        }
    }

    // [핵심] 캐시된 목록에서 필터링만 수행 (네트워크 호출 없음)
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

    // [핵심] 여러 이미지를 순차적으로 업로드하고, 마지막에 한번만 목록을 갱신하는 함수
    fun uploadClothItems(context: Context, imageUris: List<Uri>) {
        viewModelScope.launch {
            try {
                // 선택된 모든 이미지에 대해 반복
                for (uri in imageUris) {
                    val fileStream = context.contentResolver.openInputStream(uri)
                    val fileBytes = fileStream?.readBytes()
                    fileStream?.close()

                    if (fileBytes != null) {
                        val requestBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

                        // Repository를 호출하여 업로드. 성공/실패 여부만 확인.
                        closetRepository.createCloth(imagePart)
                            .onFailure {
                                Log.e("ClosetDebug", "이미지 업로드 실패: $uri, 사유: ${it.message}")
                            }
                    }
                }

                // [가장 중요] 모든 업로드 시도가 끝난 후, '전체' 목록을 딱 한 번만 새로고침.
                loadAllClosetItems()

            } catch (e: Exception) {
                Log.e("ClosetDebug", "이미지 처리 중 오류 발생", e)
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
        isSelected: Boolean,
    ) {
        if (isSelected) {
            // 이 로직은 updateCategorySelection에서만 사용되므로 ViewModel에서는 필요하지 않습니다.
        } else {
            // 이 로직도 마찬가지로 필요하지 않습니다.
        }
    }
}
