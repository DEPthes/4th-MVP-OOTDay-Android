package com.bottari.ootday.presentation.view.LoginView

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher // TextWatcher 임포트
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
import com.bottari.ootday.domain.repository.AuthRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginViewBinding

    // ViewModel 인스턴스 생성 (by viewModels()는 ViewModelProvider를 편리하게 사용)
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository()) // AuthRepository를 생성자로 전달
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateLoginButtonState()

        // 로그인 입력창
        binding.loginArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState() // 텍스트 변경 시마다 버튼 상태 업데이트
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 비밀번호 입력창
        binding.passwordArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState() // 텍스트 변경 시마다 버튼 상태 업데이트
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
                    // 메인 화면(MainActivity)으로 이동
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
                }
            }
        })

        // 2. 로딩 상태 관찰 (예: 로딩 스피너 표시/숨김)
        loginViewModel.isLoading.observe(this, Observer { isLoading ->
            // TODO: 로딩 스피너, ProgressBar 등을 표시하거나 숨기는 로직 추가
            // 예시: binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loginButton.isEnabled = !isLoading // 로딩 중에는 버튼 비활성화
        })

        // 3. 에러 메시지 관찰
        loginViewModel.errorMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                if (message.isNotEmpty()) { // 메시지가 비어있지 않으면 오류 표시
                    // 아이디 및 비밀번호 EditText에 오류 메시지 설정 (밑줄 색상 변경 및 텍스트 표시)
                    setEditTextUnderlineColor(binding.loginArea, R.color.font_red) // 밑줄 색상 변경
                    setEditTextUnderlineColor(binding.passwordArea, R.color.font_red) // 밑줄 색상 변경
                    binding.loginError.setText(message)
                } else { // 메시지가 비어있으면 오류 지우기 (초기화 또는 성공 시)
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

    // 로그인 필드가 유효한지(비어있지 않은지) 확인하는 함수
    private fun isLoginFieldsValid(): Boolean {
        val username = binding.loginArea.text.toString().trim()
        val password = binding.passwordArea.text.toString().trim()
        return username.isNotEmpty() && password.isNotEmpty()
    }

    // 로그인 버튼의 활성화 상태를 업데이트하는 함수
    private fun updateLoginButtonState() {
        val isValid = isLoginFieldsValid()
        // ViewModel의 isLoading 상태도 함께 고려하여 버튼 활성화 여부 결정
        binding.loginButton.isEnabled = isValid && !(loginViewModel.isLoading.value ?: false)
    }

    private fun clearLoginErrors() {
        binding.loginArea.setError(null)
        binding.passwordArea.setError(null)

        setEditTextUnderlineColor(binding.loginArea, R.color.gray_dark) // 원래 색상으로 복원
        setEditTextUnderlineColor(binding.passwordArea, R.color.gray_dark) // 원래 색상으로 복원
        binding.loginError.setText("")
    }
}