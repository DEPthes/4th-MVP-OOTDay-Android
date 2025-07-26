package com.bottari.ootday.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // 사용자 입력 LiveData
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rememberMe = MutableLiveData<Boolean>(false)

    // UI 상태 LiveData
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoginButtonEnabled = MutableLiveData<Boolean>(false)
    val isLoginButtonEnabled: LiveData<Boolean> = _isLoginButtonEnabled

    // 네비게이션 이벤트를 위한 LiveData
    private val _navigateToSignup = MutableLiveData<Boolean>()
    val navigateToSignup: LiveData<Boolean> = _navigateToSignup

    private val _navigateToFindId = MutableLiveData<Boolean>()
    val navigateToFindId: LiveData<Boolean> = _navigateToFindId

    private val _navigateToFindPassword = MutableLiveData<Boolean>()
    val navigateToFindPassword: LiveData<Boolean> = _navigateToFindPassword

    // 아이디/비밀번호 입력 시 버튼 상태 업데이트
    fun updateLoginButtonState() {
        _isLoginButtonEnabled.value = !username.value.isNullOrBlank() && !password.value.isNullOrBlank()
    }

    fun onLoginClick() {
        val id = username.value
        val pw = password.value
        val remember = rememberMe.value ?: false

        if (id.isNullOrBlank() || pw.isNullOrBlank()) {
            _errorMessage.value = "아이디와 비밀번호를 모두 입력해주세요."
            return
        }

        _isLoading.value = true
        _errorMessage.value = "" // 이전 에러 메시지 초기화

        viewModelScope.launch {
            try {
                val request = LoginRequest(id, pw)
                val success = authRepository.login(request)

                if (success) {
                    // 로그인 유지 옵션이 체크되어 있으면 상태 저장
                    if (remember) {
                        authRepository.saveLoginState(true)
                    }
                    _loginResult.value = true
                } else {
                    _errorMessage.value = "로그인 실패: 아이디 또는 비밀번호가 올바르지 않습니다."
                    _loginResult.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
                _loginResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 네비게이션 이벤트 처리
    fun onSignupClick() {
        _navigateToSignup.value = true
    }

    fun onFindIdClick() {
        _navigateToFindId.value = true
    }

    fun onFindPasswordClick() {
        _navigateToFindPassword.value = true
    }

    // 네비게이션 이벤트 완료 처리
    fun onSignupNavigated() {
        _navigateToSignup.value = false
    }

    fun onFindIdNavigated() {
        _navigateToFindId.value = false
    }

    fun onFindPasswordNavigated() {
        _navigateToFindPassword.value = false
    }

    // 에러 메시지 표시 완료 처리
    fun onErrorMessageShown() {
        _errorMessage.value = ""
    }
}