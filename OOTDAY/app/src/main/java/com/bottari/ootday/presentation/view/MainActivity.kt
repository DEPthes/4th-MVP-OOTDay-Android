package com.bottari.ootday.presentation.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bottari.ootday.domain.repository.AuthRepository
import com.bottari.ootday.presentation.view.LoginView.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository()

        // 로그인 상태 확인 후 적절한 화면으로 라우팅
        if (authRepository.isLoggedIn()) {
            // 이미 로그인되어 있으면 홈 화면으로
            navigateToHome()
        } else {
            // 로그인이 필요하면 로그인 화면으로
            navigateToLogin()
        }

        // MainActivity는 라우팅 역할만 하므로 finish()
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        // TODO: HomeActivity 생성 후 구현
        // val intent = Intent(this, HomeActivity::class.java)
        // startActivity(intent)

        // 임시로 로그인 화면으로 이동
        navigateToLogin()
    }
}