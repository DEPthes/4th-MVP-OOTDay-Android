package com.bottari.ootday.data.model.mainModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.ItemRepository

class SecondClosetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecondClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // ItemRepository를 생성하여 ViewModel에 전달
            return SecondClosetViewModel(ItemRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
