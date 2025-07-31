package com.bottari.ootday.presentation.view.loginView

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType // InputType 임포트
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import com.bottari.ootday.R
import com.bottari.ootday.data.model.loginModel.LoginRequest
import com.bottari.ootday.data.model.loginModel.LoginViewModel
import com.bottari.ootday.data.model.loginModel.LoginViewModelFactory
import com.bottari.ootday.databinding.LoginActivityBinding
import com.bottari.ootday.domain.repository.AuthRepository
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository())
    }
    private var defaultEditTextTint: Int = 0
    private lateinit var notoSansKrRegular: Typeface

    // 비밀번호 보이기/숨기기 상태를 추적하는 변수
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notoSansKrRegular = ResourcesCompat.getFont(this, R.font.noto_sans_kr_regular)!!
        defaultEditTextTint = ContextCompat.getColor(this, R.color.gray_dark)

        updateLoginButtonState()

        binding.loginArea.addTextChangedListener(createErrorClearingTextWatcher())
        binding.passwordArea.addTextChangedListener(createErrorClearingTextWatcher())

        binding.passwordArea.typeface = notoSansKrRegular

        binding.loginButton.setOnClickListener {
            val user = LoginRequest(binding.loginArea.text.toString(), binding.passwordArea.text.toString())
            loginViewModel.login(user)
        }

        // 비밀번호 보이기/숨기기 버튼 클릭 리스너
        binding.passwordToggleButton.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.textSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // ViewModel의 LiveData 관찰
        loginViewModel.loginResult.observe(
            this,
            Observer { event ->
                event.getContentIfNotHandled()?.let { success ->
                    if (success) {
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                        clearLoginErrors()
                    }
                }
            },
        )

        loginViewModel.isLoading.observe(
            this,
            Observer { isLoading ->
                binding.loginButton.isEnabled = !isLoading && isLoginFieldsValid()
            },
        )

        loginViewModel.errorMessage.observe(
            this,
            Observer { event ->
                event.getContentIfNotHandled()?.let { message ->
                    if (message.isNotEmpty()) {
                        setEditTextUnderlineColor(binding.loginArea, R.color.font_red)
                        setEditTextUnderlineColor(binding.passwordArea, R.color.font_red)
                        binding.loginError.setText(message)
                        setLoginErrorVisibility(true) // 오류 메시지 보이게
                        binding.loginArea.requestFocus()
                    } else {
                        clearLoginErrors()
                    }
                }
            },
        )
    }

    private fun setEditTextUnderlineColor(
        editText: EditText,
        colorResId: Int,
    ) {
        val color = ContextCompat.getColor(this, colorResId)
        val wrappedDrawable = DrawableCompat.wrap(editText.background)
        DrawableCompat.setTint(wrappedDrawable, color)
        editText.background = wrappedDrawable
    }

    // 오류 메시지 TextView를 직접 표시/숨기는 함수
    private fun setLoginErrorVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.loginError.visibility = View.VISIBLE
        } else {
            binding.loginError.text = "" // 텍스트 지우기
            binding.loginError.visibility = View.GONE
        }
    }

    // 비밀번호 보이기/숨기기 토글 로직
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible // 상태 반전

        if (isPasswordVisible) {
            // 비밀번호 보이게: text visible password
            binding.passwordArea.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordToggleButton.setImageResource(R.drawable.btn_ps_visible_on) // 눈 뜨인 아이콘
        } else {
            // 비밀번호 숨김: text password
            binding.passwordArea.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordToggleButton.setImageResource(R.drawable.btn_ps_visible_off) // 눈 감긴 아이콘
        }
        // visible on/off 시 font가 default로 설정되기에 다시 원래 font로 설정
        binding.passwordArea.typeface = notoSansKrRegular
    }

    // TextWatcher 헬퍼 함수: 입력 시 오류 지우기
    private fun createErrorClearingTextWatcher(): TextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                updateLoginButtonState()
                // 에러 메시지가 현재 보이는 상태일 경우에만 clearLoginErrors 호출
                if (binding.loginError.visibility == View.VISIBLE) {
                    clearLoginErrors()
                } else { // 밑줄 색상이 빨간색일 때도 오류 초기화 (EditText만 수정했을 경우)
                    val redColor = ContextCompat.getColor(this@LoginActivity, R.color.font_red)
                    val currentLoginAreaTint = binding.loginArea.backgroundTintList?.defaultColor ?: Color.TRANSPARENT
                    val currentPasswordAreaTint = binding.passwordArea.backgroundTintList?.defaultColor ?: Color.TRANSPARENT

                    if (currentLoginAreaTint == redColor || currentPasswordAreaTint == redColor) {
                        clearLoginErrors()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    private fun isLoginFieldsValid(): Boolean {
        val username =
            binding.loginArea.text
                .toString()
                .trim()
        val password =
            binding.passwordArea.text
                .toString()
                .trim()
        return username.isNotEmpty() && password.isNotEmpty()
    }

    private fun updateLoginButtonState() {
        val isValid = isLoginFieldsValid()
        binding.loginButton.isEnabled = isValid && !(loginViewModel.isLoading.value ?: false)
    }

    // 모든 오류 UI를 초기화하는 함수
    private fun clearLoginErrors() {
        setEditTextUnderlineColor(binding.loginArea, R.color.gray_dark)
        setEditTextUnderlineColor(binding.passwordArea, R.color.gray_dark)
        setLoginErrorVisibility(false)
    }
}
