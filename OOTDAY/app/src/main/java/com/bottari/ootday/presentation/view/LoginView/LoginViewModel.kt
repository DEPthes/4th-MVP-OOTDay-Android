package com.bottari.ootday.presentation.view.LoginView

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.model.LoginRequest // Model 계층에서 정의될 데이터 클래스
import com.bottari.ootday.data.repository.AuthRepository // Model 계층에서 정의될 Repository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // 사용자 입력 LiveData
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rememberMe = MutableLiveData<Boolean>(false) // 기본값 false

    // UI 상태 LiveData (View에서 관찰)
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 로그인 버튼 활성화 상태 (아이디/비밀번호 입력 시 활성화)
    private val _isLoginButtonEnabled = MutableLiveData<Boolean>(false)
    val isLoginButtonEnabled: LiveData<Boolean> = _isLoginButtonEnabled

    // 아이디/비밀번호 입력 시 버튼 상태 업데이트 로직
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
        viewModelScope.launch {
            try {
                // 로그인 API 호출 (AuthRepository를 통해 Model 계층과 통신)
                val request = LoginRequest(id, pw)
                val success = authRepository.login(request) // 가상의 로그인 함수

                if (success) {
                    _loginResult.value = true // 로그인 성공
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

    // 다른 클릭 리스너 (회원가입, 아이디 찾기, 비밀번호 찾기)
    fun onSignupClick() {
        // 회원가입 화면으로 이동하는 이벤트를 View에 알립니다.
        _errorMessage.value = "회원가입 버튼 클릭됨" // 예시
    }

    fun onFindIdClick() {
        // 아이디 찾기 화면으로 이동하는 이벤트를 View에 알립니다.
        _errorMessage.value = "아이디 찾기 버튼 클릭됨" // 예시
    }

    fun onFindPasswordClick() {
        // 비밀번호 찾기 화면으로 이동하는 이벤트를 View에 알립니다.
        _errorMessage.value = "비밀번호 찾기 버튼 클릭됨" // 예시
    }
}