package com.bottari.ootday.data.model.signupModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// SignUpStep1Fragment를 위한 ViewModel
class SignUpStep2ViewModel : ViewModel() {
    // UI 상태나 데이터를 LiveData로 관리
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _isValidInput = MutableLiveData<Boolean>()
    val isValidInput: LiveData<Boolean> get() = _isValidInput

    // 사용자 입력 처리 및 유효성 검사 로직
    fun onNextButtonClicked(inputName: String) {
        // 여기에 이름 유효성 검사 로직 구현
        // 예: if (inputName.length in 2..5) {
        //         _isValidInput.value = true
        //         _userName.value = inputName
        //     } else {
        //         _isValidInput.value = false
        //     }
    }
}
