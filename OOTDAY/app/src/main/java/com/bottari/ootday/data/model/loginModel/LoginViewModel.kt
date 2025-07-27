package com.bottari.ootday.data.model.loginModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    // 사용자 입력 LiveData
    val user = MutableLiveData<LoginRequest>()
    val rememberMe = MutableLiveData<Boolean>(false)

    // UI 상태 LiveData
    private val _loginResult = MutableLiveData<Event<Boolean>>() // 로그인 성공/실패 여부 (Event 래퍼 사용)
    val loginResult: LiveData<Event<Boolean>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>() // 로딩 상태
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<Event<String>>() // 에러 메시지
    val errorMessage: LiveData<Event<String>> = _errorMessage

    fun login(user: LoginRequest) {
        _isLoading.value = true // 로그인 시작 시 로딩 상태 true

        // 코루틴을 사용하여 비동기 작업 수행
        viewModelScope.launch {
            val result = authRepository.login(user.username, user.password)
            _isLoading.value = false // 로그인 완료 시 로딩 상태 false

            result
                .onSuccess { success ->
                    _loginResult.value = Event(success) // 로그인 성공 이벤트 발생
                }.onFailure { exception ->
                    _errorMessage.value = Event(exception.message ?: "알 수 없는 오류 발생") // 에러 메시지 이벤트 발생
                }
        }
    }
}

// LiveData의 일회성 이벤트를 처리하기 위한 래퍼 클래스 (옵션)
// Toast 메시지나 SnackBar 등 한 번만 소비되어야 하는 이벤트를 처리할 때 유용합니다.
open class Event<out T>(
    private val content: T,
) {
    var hasBeenHandled = false
        private set // 외부에서 변경 불가

    /**
     * 콘텐츠를 반환하고, 이미 처리되었다면 null을 반환합니다.
     */
    fun getContentIfNotHandled(): T? =
        if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }

    /**
     * 콘텐츠의 값을 처리 여부와 상관없이 반환합니다.
     */
    fun peekContent(): T = content
}
