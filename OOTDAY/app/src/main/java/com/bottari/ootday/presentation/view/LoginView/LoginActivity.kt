package com.bottari.ootday.presentation.view.LoginView


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bottari.ootday.databinding.LoginViewBinding
import com.bottari.ootday.presentation.view.MainActivity

class LoginActivity : AppCompatActivity() {

    // View Binding 사용 시
    private lateinit var binding: LoginViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding 사용
        binding = LoginViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // View Binding 미사용 시
        // setContentView(R.layout.login_view) // login_view.xml 레이아웃 사용

        // View Binding 사용 시
        val usernameEditText: EditText = binding.loginArea // login_view.xml에 정의된 ID
        val passwordEditText: EditText = binding.passwordArea // login_view.xml에 정의된 ID
        val loginButton: Button = binding.loginButton // login_view.xml에 정의된 ID

        // View Binding 미사용 시
        // val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        // val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        // val loginButton: Button = findViewById(R.id.loginButton)


        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // 실제 로그인 로직을 여기에 구현합니다 (예: 서버 API 호출, 데이터베이스 확인 등)
            if (performLogin(username, password)) {
                // 메인 화면(MainActivity)으로 이동
                val intent = Intent(this, MainActivity::class.java)
                // 로그인 액티비티는 스택에서 제거하여 뒤로 가기 시 다시 나타나지 않도록 합니다.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 로그인 인증을 수행하는 가상의 함수
    private fun performLogin(username: String, password: String): Boolean {
        return username == "test" && password == "1234"
    }
}