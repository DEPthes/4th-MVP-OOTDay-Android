package com.bottari.ootday.data.model.profileModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.ClosetRepository

class ProfileClosetViewModelFactory(private val repository: ClosetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileClosetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileClosetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}