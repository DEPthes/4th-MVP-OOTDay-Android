package com.bottari.ootday.presentation.view.mainView.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.bottomNavigation.setOnApplyWindowInsetsListener(null)

        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // bottom_navigation와 navController 연결
        binding.bottomNavigation.setupWithNavController(navController)

        // 내비게이션 목적지가 바뀔 때마다 백 버튼을 보이거나 숨기는 리스너 추가
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // when 구문을 사용하여 특정 화면에서 백 버튼을 숨김
            when (destination.id) {
                R.id.homeFragment,
                R.id.secondClosetLoadingFragment, // 로딩 화면일 때
                R.id.secondClosetResultFragment,
                R.id.feedFragment,
                R.id.profileFragment
                -> { // 결과 화면일 때
                    binding.mainBackButton.visibility = View.GONE
                }
                // 그 외 모든 Fragment에서는 백 버튼을 보여줌
                else -> {
                    binding.mainBackButton.visibility = View.VISIBLE
                }
            }
        }

        // 백 버튼 클릭 시 이전 화면으로 이동
        binding.mainBackButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
