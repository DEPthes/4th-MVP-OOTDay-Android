package com.bottari.ootday.data.model.signupModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.model.loginModel.Event
import com.bottari.ootday.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpStep2ViewModel(application: Application) : AndroidViewModel(application) {
    // (기존 코드 유지)
    private val authRepository: AuthRepository = AuthRepository(application.applicationContext)

    private var currentPhoneNumber = ""
    private var currentAuthCode = ""

    private val _isGetAuthButtonEnabled = MutableLiveData<Boolean>()
    val isGetAuthButtonEnabled: LiveData<Boolean> get() = _isGetAuthButtonEnabled

    private val _isCheckAuthButtonEnabled = MutableLiveData<Boolean>()
    val isCheckAuthButtonEnabled: LiveData<Boolean> get() = _isCheckAuthButtonEnabled

    private val _isNextButtonEnabled = MutableLiveData<Boolean>()
    val isNextButtonEnabled: LiveData<Boolean> get() = _isNextButtonEnabled

    private val _isAuthCodeInputEnabled = MutableLiveData<Boolean>(false)
    val isAuthCodeInputEnabled: LiveData<Boolean> get() = _isAuthCodeInputEnabled

    private val _isAuthSuccessful = MutableLiveData<Boolean>(false)
    val isAuthSuccessful: LiveData<Boolean> get() = _isAuthSuccessful

    private val _authCodeErrorMessage = MutableLiveData<String?>()
    val authCodeErrorMessage: LiveData<String?> get() = _authCodeErrorMessage

    private val _eventMessage = MutableLiveData<Event<String>>()
    val eventMessage: LiveData<Event<String>> get() = _eventMessage

    private val _timerText = MutableLiveData<String?>()
    val timerText: LiveData<String?> get() = _timerText

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var timerJob: Job? = null
    private val totalTime = 180 // 3분

    fun onPhoneNumberChanged(phoneNumber: String) {
        currentPhoneNumber = phoneNumber
        updateButtonStates()
    }

    fun onAuthCodeChanged(authCode: String) {
        currentAuthCode = authCode
        updateButtonStates()
    }


    private fun updateButtonStates() {
        // 인증받기 버튼 활성화 로직
        _isGetAuthButtonEnabled.value = currentPhoneNumber.length >= 10 && currentPhoneNumber.startsWith("010")

        // 인증확인 버튼 활성화 로직
        _isCheckAuthButtonEnabled.value = currentAuthCode.length == 6 && _isAuthCodeInputEnabled.value == true

        // 다음 버튼 활성화 로직
        _isNextButtonEnabled.value = _isAuthSuccessful.value == true
    }

//    //mocking 코드
//    fun onRequestAuthClick() {
//        if (currentPhoneNumber.length >= 3) {
//            _isLoading.value = true
//            viewModelScope.launch {
//                // 백엔드 명세가 나오기 전까지 사용할 Mocking 코드
//                // 어떤 번호를 입력해도 성공 응답을 반환합니다.
//                val mockedResponse = authRepository.requestPhoneNumberAuth(currentPhoneNumber)
//                mockedResponse
//                    .onSuccess {
//                        _isLoading.value = false
//                        _eventMessage.value = Event("인증번호가 발송되었습니다.")
//                        _isAuthCodeInputEnabled.value = true // 이 부분: 인증번호 입력창 활성화
//                        _isCheckAuthButtonEnabled.value = false // 초기 인증번호 입력 전까지 비활성화
//                        _isGetAuthButtonEnabled.value = false // 인증번호 받기 버튼 비활성화
//                        startAuthTimer()
//                        _isAuthSuccessful.value = false
//                        _authCodeErrorMessage.value = null
//                        updateButtonStates()
//                    }.onFailure {
//                        _isLoading.value = false
//                        _eventMessage.value = Event("인증번호 요청 실패: ${it.message ?: "알 수 없는 오류"}")
//                        setAuthFailedState()
//                    }
//            }
//        } else {
//            _eventMessage.value = Event("휴대폰 번호를 3자 이상 입력해주세요.")
//        }
//    }

//    실제 백엔드 연결 코드
    fun onRequestAuthClick() {
        if (currentPhoneNumber.length >= 3) {
            _isLoading.value = true
            viewModelScope.launch {
                authRepository.requestPhoneNumberAuth(currentPhoneNumber)
                    .onSuccess {
                        // API 호출 성공
                        _isLoading.value = false
                        _eventMessage.value = Event("인증번호가 발송되었습니다.")
                        _isAuthCodeInputEnabled.value = true
                        _isCheckAuthButtonEnabled.value = false
                        _isGetAuthButtonEnabled.value = false
                        startAuthTimer()
                        _isAuthSuccessful.value = false
                        _authCodeErrorMessage.value = null
                        updateButtonStates()
                    }
                    .onFailure { exception ->
                        // API 호출 실패
                        _isLoading.value = false
                        _eventMessage.value = Event("인증번호 요청 실패: ${exception.message ?: "알 수 없는 오류"}")
                        setAuthFailedState()
                    }
            }
        } else {
            _eventMessage.value = Event("휴대폰 번호를 3자 이상 입력해주세요.")
        }
    }

//    // 인증번호 확인 mocking코드
//    fun onCheckAuthClick() {
//        if (currentAuthCode.length == 6) {
//            _isLoading.value = true
//            viewModelScope.launch {
//                // 백엔드 명세가 나오기 전까지 사용할 Mocking 코드
//                // 입력된 인증번호가 "123456"일 경우에만 성공으로 간주합니다.
//                val isCodeValid = currentAuthCode == "123456"
//                delay(500) // 가짜 네트워크 지연
//                _isLoading.value = false
//                if (isCodeValid) {
//                    _eventMessage.value = Event("인증되었습니다.")
//                    _isAuthSuccessful.value = true
//                    _authCodeErrorMessage.value = null
//                    timerJob?.cancel() // 인증 성공 시 타이머 중지
//
//                    // 인증 성공 시 모든 UI를 비활성화하고, 다음 버튼만 활성화
//                    _isGetAuthButtonEnabled.value = false // 인증받기 버튼 비활성화
//                    _isCheckAuthButtonEnabled.value = false // 인증 확인 버튼 비활성화
//                    _isAuthCodeInputEnabled.value = false // 인증번호 입력 EditText 비활성화
//                    _isNextButtonEnabled.value = true // 다음 버튼 활성화
//                    _timerText.value = null // 타이머 숨김
//                } else {
//                    _authCodeErrorMessage.value = "인증 번호가 일치하지 않아요. 다시 입력해 주세요."
//                    _isAuthSuccessful.value = false
//                    updateButtonStates() // 인증 실패 시에만 버튼 상태를 업데이트하도록 유지
//                }
//            }
//        } else {
//            _authCodeErrorMessage.value = "인증번호 6자리를 모두 입력해주세요."
//        }
//    }


//    실제 백엔드 인증번호 확인 코드
    fun onCheckAuthClick() {
        if (currentAuthCode.length == 6) {
            _isLoading.value = true
            viewModelScope.launch {
                authRepository.checkPhoneNumberAuth(currentPhoneNumber, currentAuthCode)
                    .onSuccess {
                        // 인증 성공
                        _isLoading.value = false
                        _eventMessage.value = Event("인증되었습니다.")
                        _isAuthSuccessful.value = true
                        _authCodeErrorMessage.value = null
                        timerJob?.cancel()

                        _isGetAuthButtonEnabled.value = false
                        _isCheckAuthButtonEnabled.value = false
                        _isAuthCodeInputEnabled.value = false
                        _isNextButtonEnabled.value = true
                        _timerText.value = null
                    }
                    .onFailure { exception ->
                        // 인증 실패
                        _isLoading.value = false
                        _authCodeErrorMessage.value = exception.message ?: "인증 번호가 일치하지 않아요. 다시 입력해 주세요."
                        _isAuthSuccessful.value = false
                        updateButtonStates()
                    }
            }
        } else {
            _authCodeErrorMessage.value = "인증번호 6자리를 모두 입력해주세요."
        }
    }

    private fun startAuthTimer() {
        // (기존 코드 유지)
        timerJob?.cancel()
        timerJob =
            viewModelScope.launch {
                for (i in totalTime downTo 0) {
                    val minutes = i / 60
                    val seconds = i % 60
                    _timerText.value = String.format("%02d:%02d", minutes, seconds)
                    delay(1000)
                    if (i == 0) {
                        // 타이머 종료 시 인증 실패 상태로 전환
                        setAuthFailedState()
                        _eventMessage.value = Event("인증 시간이 초과되었습니다. 다시 시도해주세요.")
                    }
                }
            }
    }

    private fun setAuthFailedState() {
        // (기존 코드 유지)
        _isAuthSuccessful.value = false
        _isCheckAuthButtonEnabled.value = false
        _isGetAuthButtonEnabled.value = true
        _isAuthCodeInputEnabled.value = false
        _timerText.value = null
        timerJob?.cancel()
        updateButtonStates()
    }
}
