package com.bottari.ootday.data.model.signupModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpStep3ViewModel : ViewModel() {
    private val _inputId = MutableLiveData<String>("")
    val inputId: LiveData<String> get() = _inputId

    private val _isNextButtonEnabled = MutableLiveData<Boolean>(false)
    val isNextButtonEnabled: LiveData<Boolean> get() = _isNextButtonEnabled

    private val _displayErrorMessage = MutableLiveData<String?>(null)
    val displayErrorMessage: LiveData<String?> get() = _displayErrorMessage

    fun onInputIdChanged(id: String) {
        _inputId.value = id
        checkLengthValidity(id)

        if (id.length in 5..20) {
            _displayErrorMessage.value = null
        } else if (id.isNotBlank()) {
            _displayErrorMessage.value = "아이디를 5 ~ 20자 이내로 입력해주세요."
        } else {
            _displayErrorMessage.value = null
        }
    }

    private fun checkLengthValidity(id: String) {
        _isNextButtonEnabled.value = id.length in 5..20
    }

    fun onNextButtonClicked(): Boolean {
        val currentId = _inputId.value ?: ""

        if (currentId.length < 5 || currentId.length > 20) {
            _displayErrorMessage.value = "아이디를 5 ~ 20자 이내로 입력해주세요."
            _isNextButtonEnabled.value = false
            return false
        }

        // 영문과 숫자 모두 포함하는지 검사
        val hasLetter = currentId.any { it.isLetter() }
        val hasDigit = currentId.any { it.isDigit() }
        val hasOnlyLettersAndDigits = currentId.all { it.isLetterOrDigit() }

        if (!hasLetter || !hasDigit) {
            _displayErrorMessage.value = "영문과 숫자를 모두 포함해야 합니다."
            _isNextButtonEnabled.value = false
            return false
        }

        if (!hasOnlyLettersAndDigits) {
            _displayErrorMessage.value = "영문과 숫자만 입력 가능합니다."
            _isNextButtonEnabled.value = false
            return false
        }

        _displayErrorMessage.value = null
        _isNextButtonEnabled.value = true
        return true
    }
}
