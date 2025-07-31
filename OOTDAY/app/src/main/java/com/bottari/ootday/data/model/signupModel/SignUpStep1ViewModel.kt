package com.bottari.ootday.data.model.signupModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpStep1ViewModel : ViewModel() {

    // 현재 EditText에 입력된 이름
    private val _inputName = MutableLiveData<String>("")
    val inputName: LiveData<String> get() = _inputName

    // '다음' 버튼의 활성화 여부 (길이 유효성만으로 결정)
    private val _isNextButtonEnabled = MutableLiveData<Boolean>(false)
    val isNextButtonEnabled: LiveData<Boolean> get() = _isNextButtonEnabled

    // 사용자에게 표시할 오류 메시지 (길이 오류 또는 형식 오류)
    private val _displayErrorMessage = MutableLiveData<String?>(null)
    val displayErrorMessage: LiveData<String?> get() = _displayErrorMessage

    /**
     * EditText 텍스트 변경 시 호출되어 길이 유효성만 검사하고 LiveData를 업데이트합니다.
     * 텍스트 형식(영문/한글 외 문자) 검사는 '다음' 버튼 클릭 시 이루어집니다.
     * @param name EditText에 새로 입력된 텍스트
     */
    fun onInputNameChanged(name: String) {
        _inputName.value = name // 입력값 업데이트
        checkLengthValidity(name) // 길이 유효성 검사

        // 텍스트를 입력하는 도중에는 형식 오류 메시지를 숨기고, 길이 오류 메시지만 표시
        // 사용자가 다시 입력하기 시작하면 이전 형식 오류는 숨겨져야 합니다.
        if (name.length in 2..5) {
            _displayErrorMessage.value = null // 길이 유효하면 에러 메시지 초기화
        } else if (name.isNotBlank()) {
            _displayErrorMessage.value = "이름을 2~5자로 입력해주세요."
        } else {
            _displayErrorMessage.value = null // 아무것도 입력 안 했을 때는 에러 없음
        }
    }

    /**
     * 이름 길이 유효성만 검사하여 '다음' 버튼의 활성화 여부를 결정합니다.
     * @param name 현재 이름
     */
    private fun checkLengthValidity(name: String) {
        _isNextButtonEnabled.value = name.length in 2..5
    }

    /**
     * '다음' 버튼 클릭 시 호출되어 최종 유효성 검사(길이 + 형식)를 수행합니다.
     * @return 모든 유효성 검사를 통과하여 다음 단계로 진행 가능한지 여부
     */
    fun onNextButtonClicked(): Boolean {
        val currentName = _inputName.value ?: ""

        // 1. 길이 유효성 재확인 (버튼이 활성화된 상태에서만 호출되지만 안전을 위해)
        if (currentName.length < 2 || currentName.length > 5) {
            _displayErrorMessage.value = "이름을 2~5자로 입력해주세요."
            _isNextButtonEnabled.value = false // 혹시라도 활성화되어 있었다면 비활성화
            return false
        }

        // 2. 영문 또는 한글 외 문자 포함 여부 검사
        // 정규식: ^[a-zA-Z가-힣]+$
        // ^: 문자열의 시작
        // [a-zA-Z가-힣]: 영어 알파벳 (대소문자) 또는 한글 (가-힣)
        // +: 하나 이상 반복
        // $: 문자열의 끝
        val regex = Regex("^[a-zA-Z가-힣]+$")
        if (!regex.matches(currentName)) {
            _displayErrorMessage.value = "숫자, 특수문자, 띄어쓰기는 사용할 수 없어요."
            return false
        }

        // 모든 유효성 검사 통과
        _displayErrorMessage.value = null // 오류 없으면 메시지 제거
        return true
    }
}