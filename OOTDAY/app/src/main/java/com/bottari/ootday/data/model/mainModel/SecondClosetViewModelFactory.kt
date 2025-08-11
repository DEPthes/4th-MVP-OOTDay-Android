package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SecondClosetViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecondClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecondClosetViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}