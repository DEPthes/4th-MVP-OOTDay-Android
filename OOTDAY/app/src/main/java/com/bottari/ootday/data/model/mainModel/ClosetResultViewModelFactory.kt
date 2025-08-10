package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.presentation.viewmodel.ClosetResultViewModel

// ... ViewModelFactory는 기존과 동일 ...
class ClosetResultViewModelFactory(private val repository: ClosetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClosetResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClosetResultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}