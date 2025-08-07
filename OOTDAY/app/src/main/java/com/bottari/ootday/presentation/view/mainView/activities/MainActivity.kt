package com.bottari.ootday.presentation.view.mainView.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bottari.ootday.R
import com.bottari.ootday.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: MainActivityBinding // 데이터 바인딩을 사용한다고 가정합니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // bottom_navigation와 navController 연결
        binding.bottomNavigation.setupWithNavController(navController)

        // 내비게이션 목적지가 바뀔 때마다 백 버튼을 보이거나 숨기는 리스너 추가
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment ) {
                // HomeFragment일 경우 백 버튼 숨기기
                binding.mainBackButton.visibility = View.GONE
            } else {
                // HomeFragment가 아닐 경우 백 버튼 보이기
                binding.mainBackButton.visibility = View.VISIBLE
            }
        }

        // 백 버튼 클릭 시 이전 화면으로 이동
        binding.mainBackButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (!navController.navigateUp()) {
            super.onBackPressed()
        }
    }
}