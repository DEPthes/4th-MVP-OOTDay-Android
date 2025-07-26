package com.bottari.ootday.presentation.view.LoginView

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View // View 임포트
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bottari.ootday.R
import com.bottari.ootday.data.model.LoginModel.LoginRequest
import com.bottari.ootday.databinding.LoginViewBinding
import com.bottari.ootday.data.model.LoginModel.LoginViewModel
import com.bottari.ootday.data.model.LoginModel.LoginViewModelFactory
import com.bottari.ootday.domain.repository.AuthRepository // AuthRepository 경로

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginViewBinding

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository())
    }

    private var defaultEditTextTint: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        defaultEditTextTint = ContextCompat.getColor(this, R.color.gray_dark)

        updateLoginButtonState()

        // 로그인 입력창 - TextWatcher 추가: 입력 시 오류 UI 초기화
        binding.loginArea.addTextChangedListener(createErrorClearingTextWatcher())

        // 비밀번호 입력창 - TextWatcher 추가: 입력 시 오류 UI 초기화
        binding.passwordArea.addTextChangedListener(createErrorClearingTextWatcher())

        // 로그인 버튼 클릭 리스너 설정
        binding.loginButton.setOnClickListener {
            val user = LoginRequest(binding.loginArea.text.toString(), binding.passwordArea.text.toString())
            loginViewModel.login(user) // ViewModel의 로그인 함수 호출
        }

        // ViewModel의 LiveData 관찰 (Observer 패턴)
        // 1. 로그인 결과 관찰
        loginViewModel.loginResult.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    clearLoginErrors() // 성공 시 오류 UI 초기화
                    // 메인 화면(MainActivity)으로 이동 관련 코드는 주석 처리 유지 (MainActivity가 없으므로)
                }
            }
        })

        // 2. 로딩 상태 관찰 (예: 로딩 스피너 표시/숨김)
        loginViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.loginButton.isEnabled = !isLoading && isLoginFieldsValid() // 로딩 중에는 버튼 비활성화, 유효성 검사도 추가
        })

        // 3. 에러 메시지 관찰
        loginViewModel.errorMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                if (message.isNotEmpty()) { // 메시지가 비어있지 않으면 오류 표시
                    // EditText 밑줄 색상 변경
                    setEditTextUnderlineColor(binding.loginArea, R.color.font_red)
                    setEditTextUnderlineColor(binding.passwordArea, R.color.font_red)
                    // 에러 메시지 TextView에 텍스트 설정
                    binding.loginError.setText(message)
                    // 에러 메시지 TextView 보이게 하기 (애니메이션 없음)
                    binding.loginError.visibility = View.VISIBLE
                    binding.loginArea.requestFocus() // 편의를 위해 첫 번째 필드에 포커스
                } else { // 메시지가 비어있으면 오류 숨기기 (초기화 또는 성공 시)
                    clearLoginErrors()
                }
            }
        })
    }

    // EditText의 밑줄 색상을 변경하는 헬퍼 함수
    private fun setEditTextUnderlineColor(editText: EditText, colorResId: Int) {
        val color = ContextCompat.getColor(this, colorResId)
        val wrappedDrawable = DrawableCompat.wrap(editText.background)
        DrawableCompat.setTint(wrappedDrawable, color)
        editText.background = wrappedDrawable
    }

    // 오류 메시지 TextView를 직접 표시/숨기는 함수 (애니메이션 없음)
    private fun setLoginErrorVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.loginError.visibility = View.VISIBLE
        } else {
            binding.loginError.text = "" // 텍스트 지우기
            binding.loginError.visibility = View.GONE
        }
    }

    // TextWatcher 헬퍼 함수: 입력 시 오류 지우기
    private fun createErrorClearingTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
                // 텍스트 입력 시 바로 오류 UI 초기화
                // 에러 메시지가 현재 보이는 상태일 경우에만 clearLoginErrors 호출
                if (binding.loginError.visibility == View.VISIBLE) {
                    clearLoginErrors()
                } else { // 밑줄 색상이 빨간색일 때도 오류 초기화 (EditText만 수정했을 경우)
                    val currentLoginAreaTint = binding.loginArea.backgroundTintList?.defaultColor ?: Color.TRANSPARENT
                    val currentPasswordAreaTint = binding.passwordArea.backgroundTintList?.defaultColor ?: Color.TRANSPARENT
                    val redColor = ContextCompat.getColor(this@LoginActivity, R.color.font_red)
                    if (currentLoginAreaTint == redColor || currentPasswordAreaTint == redColor) {
                        clearLoginErrors()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    // 로그인 필드가 유효한지(비어있지 않은지) 확인하는 함수
    private fun isLoginFieldsValid(): Boolean {
        val username = binding.loginArea.text.toString().trim()
        val password = binding.passwordArea.text.toString().trim()
        return username.isNotEmpty() && password.isNotEmpty()
    }

    // 로그인 버튼의 활성화 상태를 업데이트하는 함수
    private fun updateLoginButtonState() {
        val isValid = isLoginFieldsValid()
        binding.loginButton.isEnabled = isValid && !(loginViewModel.isLoading.value ?: false)
    }

    // 모든 오류 UI를 초기화하는 함수
    private fun clearLoginErrors() {
        // EditText 밑줄 색상 기본값으로 복원
        setEditTextUnderlineColor(binding.loginArea, R.color.gray_dark)
        setEditTextUnderlineColor(binding.passwordArea, R.color.gray_dark)
        // 오류 메시지 TextView 숨기기 (애니메이션 없음)
        setLoginErrorVisibility(false)
    }
}