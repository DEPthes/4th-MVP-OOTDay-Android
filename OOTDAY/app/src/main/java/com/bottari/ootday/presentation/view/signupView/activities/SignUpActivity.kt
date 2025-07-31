package com.bottari.ootday.presentation.view.signupView.activities // 실제 앱 패키지명으로 변경해주세요

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bottari.ootday.databinding.SignUpActivityBinding // 올바른 바인딩 클래스
import com.bottari.ootday.presentation.view.signupView.fragments.SignUpStep1Fragment
import com.bottari.ootday.presentation.view.signupView.fragments.SignUpStep2Fragment
import com.bottari.ootday.presentation.view.signupView.fragments.SignUpStep3Fragment
import com.bottari.ootday.presentation.view.signupView.fragments.SignUpStep4Fragment

// 각 Fragment들을 import 해야 합니다.

// 최종 화면 Activity import

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: SignUpActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignUpActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정 (선택 사항: ActionBar로 사용하고 싶을 경우)
        setSupportActionBar(binding.signupToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 기본 타이틀 숨기기

        // 툴바의 뒤로가기 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            // 시스템의 onBackPressed()와 동일한 동작을 수행
            onBackPressedDispatcher.onBackPressed()
        }

        // FragmentManager의 백스택 변경을 감지하여 툴바 UI 업데이트
        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarUI()
        }

        // 초기 Fragment 로드 (SignUpStep1Fragment)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(binding.signupMainFragment.id, SignUpStep1Fragment())
                .commit()
        }
        updateToolbarUI() // 초기 로드 후 툴바 UI 업데이트
    }

    /**
     * 다음 Fragment로 이동하거나, 마지막 단계일 경우 `SignUpCongActivity`로 전환합니다.
     * @param currentStep 현재 Fragment의 단계 (1부터 5까지)
     */
    fun navigateToNextStep(currentStep: Int) {
        val nextFragment: Fragment? =
            when (currentStep) {
                1 -> SignUpStep2Fragment()
                2 -> SignUpStep3Fragment()
                3 -> SignUpStep4Fragment()
                else -> null // Step 5 이후에는 Fragment 전환이 아닌 Activity 전환
            }

        if (nextFragment != null) {
            // Fragment 전환
            supportFragmentManager
                .beginTransaction()
                .replace(binding.signupMainFragment.id, nextFragment)
                .addToBackStack(null) // 뒤로가기 시 이전 Fragment로 돌아갈 수 있도록 백스택에 추가
                .commit()
        } else {
            // Step 4에서 다음 버튼 클릭 시 SignUpCongActivity로 이동
            val intent = Intent(this, SignUpCongActivity::class.java)
            startActivity(intent)
            finish() // 회원가입 흐름을 마쳤으므로 현재 SignupActivity 종료
        }
    }

    /**
     * 현재 Fragment의 단계에 따라 툴바의 뒤로가기 버튼 가시성과 페이지 인디케이터 텍스트를 업데이트합니다.
     */
    private fun updateToolbarUI() {
        val currentFragment = supportFragmentManager.findFragmentById(binding.signupMainFragment.id)
        val currentStepNum = getCurrentStepNumber(currentFragment)

        // 1. 뒤로가기 버튼 가시성 제어
        if (currentStepNum == 1) {
            // 첫 번째 Step1일 때는 뒤로가기 버튼 비활성화 (숨김)
            binding.backButton.visibility = View.INVISIBLE // 또는 View.GONE
        } else {
            // 그 외의 경우 뒤로가기 버튼 활성화 (보임)
            binding.backButton.visibility = View.VISIBLE
        }

        // 2. 페이지 인디케이터 텍스트 업데이트
        binding.pageIndicator.text = "$currentStepNum/4"
    }

    /**
     * 현재 Fragment 인스턴스에 따라 단계 번호를 반환합니다.
     */
    private fun getCurrentStepNumber(fragment: Fragment?): Int =
        when (fragment) {
            is SignUpStep1Fragment -> 1
            is SignUpStep2Fragment -> 2
            is SignUpStep3Fragment -> 3
            is SignUpStep4Fragment -> 4
            else -> 1 // 기본값 또는 알 수 없는 Fragment일 경우
        }

    // Android 시스템의 뒤로가기 버튼 동작을 재정의하려면 이 함수를 사용합니다.
    // 하지만 `onBackPressedDispatcher.onBackPressed()`를 사용하면 기본 동작을 따르므로 필수는 아님.
    // override fun onBackPressed() {
    //     if (supportFragmentManager.backStackEntryCount > 0) {
    //         // 백스택에 Fragment가 있으면 pop
    //         super.onBackPressed()
    //     } else {
    //         // 더 이상 Fragment가 없으면 Activity 종료
    //         super.onBackPressed()
    //     }
    // }
}
