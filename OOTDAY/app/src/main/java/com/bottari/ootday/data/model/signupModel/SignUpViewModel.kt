package com.bottari.ootday.data.model.signupModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {
    private val _signUpData = MutableLiveData<SignUpData>()
    val signUpData: LiveData<SignUpData> get() = _signUpData

    init {
        _signUpData.value = SignUpData()
    }

    fun setName(name: String) {
        _signUpData.value = _signUpData.value?.copy(name = name)
    }

    fun setPhoneNumber(phoneNumber: String) {
        _signUpData.value = _signUpData.value?.copy(phoneNumber = phoneNumber)
    }

    fun setId(id: String) {
        _signUpData.value = _signUpData.value?.copy(id = id)
    }

    fun setPassword(password: String) {
        _signUpData.value = _signUpData.value?.copy(password = password)
    }
}
