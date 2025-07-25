package com.bottari.ootday.presentation.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider // ViewModelProvider 임포트
import com.bottari.ootday.R
import com.bottari.ootday.data.repository.AuthRepository // Repository 임포트
import com.bottari.ootday.databinding.LoginViewBinding // View Binding 임포트
import com.bottari.ootday.presentation.viewmodel.LoginViewModel
import com.bottari.ootday.presentation.viewmodel.LoginViewModelFactory // 팩토리 클래스 임포트

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginViewBinding // View Binding 인스턴스
    private lateinit var viewModel: LoginViewModel // ViewModel 인스턴스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 뷰 바인딩 설정
        binding = LoginViewBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root가 최상위 뷰를 참조

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets -> // login_view_root 대신 binding.root
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewModel 초기화: ViewModelProvider와 Factory 사용
        // AuthRepository 인스턴스를 ViewModelFactory에 전달
        val authRepository = AuthRepository() // 실제 앱에서는 DI (Koin, Hilt)를 통해 주입받습니다.
        val viewModelFactory = LoginViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        // ----------------------------------------------------
        // View (Activity)에서 ViewModel의 LiveData 관찰 및 UI 업데이트
        // ----------------------------------------------------

        // 아이디 입력 필드 LiveData 바인딩 (양방향 바인딩은 Data Binding 사용 시 더 간단)
        binding.editTextId.setText(viewModel.username.value) // 초기값 설정
        binding.editTextId.doAfterTextChanged { editable ->
            viewModel.username.value = editable.toString()
            viewModel.updateLoginButtonState() // 입력 시 버튼 상태 업데이트
        }

        // 비밀번호 입력 필드 LiveData 바인딩
        binding.editTextPassword.setText(viewModel.password.value) // 초기값 설정
        binding.editTextPassword.doAfterTextChanged { editable ->
            viewModel.password.value = editable.toString()
            viewModel.updateLoginButtonState() // 입력 시 버튼 상태 업데이트
        }

        // 로그인 유지 체크박스 LiveData 바인딩
        binding.checkboxRememberMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.rememberMe.value = isChecked
        }
        viewModel.rememberMe.observe(this) { isChecked ->
            binding.checkboxRememberMe.isChecked = isChecked
        }


        // 로그인 결과 관찰
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                // TODO: 다음 화면으로 이동 (예: 메인 페이지)
                // val intent = Intent(this, MainActivity::class.java) // 메인 화면으로 가정
                // startActivity(intent)
                // finish()
            }
        }

        // 로딩 상태 관찰 (예: 프로그레스바 표시)
        viewModel.isLoading.observe(this) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonLogin.isEnabled = !isLoading // 로딩 중에는 버튼 비활성화
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        // 로그인 버튼 활성화 상태 관찰
        viewModel.isLoginButtonEnabled.observe(this) { isEnabled ->
            binding.buttonLogin.isEnabled = isEnabled
        }

        // ----------------------------------------------------
        // View (Activity)에서 사용자 이벤트 처리 및 ViewModel 함수 호출
        // ----------------------------------------------------

        // 로그인 버튼 클릭 리스너
        binding.buttonLogin.setOnClickListener {
            viewModel.onLoginClick()
        }

        // 하단 링크 클릭 리스너
        binding.textSignup.setOnClickListener { viewModel.onSignupClick() }
        binding.textFindId.setOnClickListener { viewModel.onFindIdClick() }
        binding.textFindPassword.setOnClickListener { viewModel.onFindPasswordClick() }

        // 초기 버튼 상태 업데이트
        viewModel.updateLoginButtonState()
    }
}