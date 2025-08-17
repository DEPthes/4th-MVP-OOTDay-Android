package com.bottari.ootday.data.model.surveyModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bottari.ootday.data.model.loginModel.Event

class HomeSurveyViewModel : ViewModel() {

    private val _selectedGender = MutableLiveData<String?>()
    private val _selectedColor = MutableLiveData<String?>()

    private val _navigateToMain = MutableLiveData<Event<Unit>>()
    val navigateToMain: LiveData<Event<Unit>> get() = _navigateToMain

    fun onGenderSelected(gender: String) {
        _selectedGender.value = gender
        checkCompletion()
    }

    fun onColorSelected(color: String) {
        _selectedColor.value = color
        checkCompletion()
    }

    private fun checkCompletion() {
        if (!_selectedGender.value.isNullOrBlank() && !_selectedColor.value.isNullOrBlank()) {
            _navigateToMain.value = Event(Unit)
        }
    }
}