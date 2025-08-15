package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.ClosetRepository

class FirstClosetViewModelFactory(
    private val repository: ClosetRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirstClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FirstClosetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
