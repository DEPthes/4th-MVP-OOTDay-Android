package com.bottari.ootday.data.model.loginModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bottari.ootday.data.repository.AuthRepository

// ViewModel을 생성할 때 필요한 의존성(여기서는 AuthRepository)을 제공하는 팩토리 클래스입니다.
class LoginViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    // ViewModel 인스턴스를 생성하는 메서드입니다.
    @Suppress("UNCHECKED_CAST") // 타입 캐스팅 경고를 무시합니다. 안전한 코드입니다.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 만약 요청된 ViewModel 클래스가 LoginViewModel과 같다면
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            // LoginViewModel의 생성자에 authRepository를 넘겨주어 인스턴스를 생성합니다.
            return LoginViewModel(authRepository) as T
        }
        // 요청된 ViewModel 클래스가 LoginViewModel이 아니라면 예외를 발생시킵니다.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
