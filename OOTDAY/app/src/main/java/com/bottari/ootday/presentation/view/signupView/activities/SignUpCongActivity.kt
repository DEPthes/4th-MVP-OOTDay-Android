package com.bottari.ootday.presentation.view.signupView.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bottari.ootday.databinding.SignUpCongActivityBinding
import com.bottari.ootday.presentation.view.loginView.LoginActivity
import com.bottari.ootday.presentation.view.surveyView.HomeSurveyActivity

class SignUpCongActivity : AppCompatActivity() {
    private lateinit var binding: SignUpCongActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpCongActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.moveToLoginButton.setOnClickListener {
            val intent = Intent(this, HomeSurveyActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
