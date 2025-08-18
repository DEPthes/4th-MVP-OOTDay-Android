package com.bottari.ootday.data.model.profileModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.model.loginModel.Event
import com.bottari.ootday.data.model.loginModel.ProfileResponse
import com.bottari.ootday.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    // UI에 표시될 프로필 정보를 담는 LiveData
    private val _userProfile = MutableLiveData<ProfileResponse>()
    val userProfile: LiveData<ProfileResponse> get() = _userProfile

    // 로딩 상태를 관리하는 LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // 에러 메시지를 전달하는 LiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>> get() = _navigateToLogin


    /**
     * 서버에서 사용자 프로필 정보를 가져오는 함수
     */
    fun fetchUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            authRepository.getUserProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "프로필을 불러오는 데 실패했습니다."
                }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.logout()
                .onSuccess { _navigateToLogin.value = Event(Unit) }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    /**
     * 회원 탈퇴를 처리하는 함수 (추가)
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.withdrawAccount()
                .onSuccess { _navigateToLogin.value = Event(Unit) }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}