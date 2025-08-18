package com.bottari.ootday.data.model.mainModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.ClosetRepository

class FirstClosetViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirstClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 2. Factory가 Repository를 직접 생성하여 ViewModel에 전달
            return FirstClosetViewModel(ClosetRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}