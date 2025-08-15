package com.bottari.ootday.data.model.signupModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpStep4ViewModel : ViewModel() {
    // LiveData for password and confirmation input
    private val _passwordInput = MutableLiveData<String>("")
    val passwordInput: LiveData<String> get() = _passwordInput

    private val _passwordConfirm = MutableLiveData<String>("")
    val passwordConfirm: LiveData<String> get() = _passwordConfirm

    // UI State LiveData
    private val _isNextButtonEnabled = MutableLiveData<Boolean>(false)
    val isNextButtonEnabled: LiveData<Boolean> get() = _isNextButtonEnabled

    private val _isConfirmVisible = MutableLiveData<Boolean>(false)
    val isConfirmVisible: LiveData<Boolean> get() = _isConfirmVisible

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> get() = _passwordError

    private val _passwordConfirmError = MutableLiveData<String?>(null)
    val passwordConfirmError: LiveData<String?> get() = _passwordConfirmError

    // 비밀번호 입력 시 TextWatcher에 의해 호출되는 함수
    fun onPasswordInputChanged(password: String) {
        _passwordInput.value = password
        // 길이 조건만 충족하면 다음 버튼 활성화
        val isValidLength = password.length in 8..20
        _isNextButtonEnabled.value = isValidLength

        if (isValidLength) {
            _passwordError.value = null
        } else if (password.isNotEmpty()) {
            _passwordError.value = "비밀번호는 8 ~ 20자 이내로 입력해주세요."
        } else {
            _passwordError.value = null
        }
    }

    // 비밀번호 확인 입력 시 TextWatcher에 의해 호출되는 함수
    fun onPasswordConfirmChanged(confirm: String) {
        _passwordConfirm.value = confirm
        // 비밀번호 확인 입력 시 에러 메시지 초기화
        _passwordConfirmError.value = null
    }

    /**
     * '다음' 버튼 클릭 시 호출되어 단계별 유효성 검사를 수행합니다.
     * @return 다음 단계로 이동할 수 있는지 여부
     */
    fun onNextButtonClicked(): Boolean {
        val currentPassword = _passwordInput.value ?: ""
        val currentConfirm = _passwordConfirm.value ?: ""

        // 1. 비밀번호 확인 입력창이 보이지 않는 상태 (첫 번째 '다음' 버튼 클릭)
        if (_isConfirmVisible.value == false) {
            val hasLetter = currentPassword.any { it.isLetter() }
            val hasDigit = currentPassword.any { it.isDigit() }
            val hasSpecial = currentPassword.any { !it.isLetterOrDigit() }

            if (currentPassword.length !in 8..20) {
                _passwordError.value = "비밀번호는 8 ~ 20자 이내로 입력해주세요."
                return false
            }

            if (!hasLetter || !hasDigit || !hasSpecial) {
                _passwordError.value = "패스워드는 특수기호, 영문, 숫자를 필수적으로 포함해야 합니다."
                return false
            }

            _isConfirmVisible.value = true // 비밀번호 확인 EditText와 버튼 보이게 하기
            _isNextButtonEnabled.value = true // 다음 버튼 활성화 상태 유지
            _passwordError.value = null // 에러 메시지 초기화
            return false
        } else {
            // 2. 비밀번호 확인 입력창이 보이는 상태 (두 번째 '다음' 버튼 클릭)
            if (currentPassword == currentConfirm) {
                _passwordConfirmError.value = null
                return true // 다음 단계로 이동
            } else {
                _passwordConfirmError.value = "비밀번호가 일치하지 않습니다."
                return false
            }
        }
    }
}
