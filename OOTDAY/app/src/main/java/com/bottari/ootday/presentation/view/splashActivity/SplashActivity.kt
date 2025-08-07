package com.bottari.ootday.presentation.view.splashActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bottari.ootday.R
import com.bottari.ootday.presentation.view.mainView.activities.MainActivity
import com.bottari.ootday.presentation.view.loginView.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // 스플래시 화면을 위한 레이아웃이 있다면 여기서 setContentView(R.layout.activity_splash)
        // 일반적으로 스플래시 화면은 테마를 통해 구현하므로 별도의 레이아웃 파일이 없을 수도 있습니다.
        // 이 경우 setContentView 호출은 필요 없습니다.

        // 일정 시간 지연 후 다음 화면으로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            // 1. 로그인 상태 확인 (예: SharedPreferences, DataStore, 또는 서버 토큰 유효성 검사)
            val isLoggedIn = checkIfUserIsLoggedIn()

            val nextIntent: Intent
            if (isLoggedIn) {
                // 2. 로그인 되어 있다면 MainActivity로 이동
                nextIntent = Intent(this, MainActivity::class.java)
            } else {
                // 3. 로그인 되어 있지 않다면 LoginActivity로 이동
                nextIntent = Intent(this, LoginActivity::class.java)
            }

            startActivity(nextIntent)
            finish() // 현재 SplashActivity는 스택에서 제거하여 뒤로 가기 시 다시 나타나지 않도록 합니다.
        }, 2000) // 2초(2000ms) 지연. 원하는 로딩 시간으로 조절하세요.
    }

    // 사용자의 로그인 상태를 확인하는 가상의 함수
    private fun checkIfUserIsLoggedIn(): Boolean = false
}
