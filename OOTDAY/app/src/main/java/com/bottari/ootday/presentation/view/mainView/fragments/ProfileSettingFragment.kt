package com.bottari.ootday.presentation.view.mainView.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bottari.ootday.data.repository.RetrofitClient
import com.bottari.ootday.data.service.MemberApiService
import com.bottari.ootday.databinding.ProfileSettingFragmentBinding
import com.bottari.ootday.domain.model.WithdrawRequest
import com.bottari.ootday.presentation.view.loginView.LoginActivity
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.DeleteAccountDialogFragment
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.LogoutDialogFragment
import kotlinx.coroutines.launch

class ProfileSettingFragment : Fragment() {
    private var _binding: ProfileSettingFragmentBinding? = null
    private val binding get() = _binding!!

    // ApiService 인스턴스 생성
    private val apiService: MemberApiService by lazy {
        RetrofitClient.createService(MemberApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ProfileSettingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 1. '로그아웃' 텍스트뷰 클릭 이벤트 처리
        binding.logoutText.setOnClickListener {
            // 2. 다이얼로그 생성 및 표시
            val dialog = LogoutDialogFragment(
                onConfirm = {
                    // 3. '로그아웃' 버튼 클릭 시 API 호출
                    logout()
                }
            )
            dialog.show(childFragmentManager, "LogoutDialog")
        }

        // '계정 탈퇴' 텍스트 클릭 이벤트 (추가)
        binding.deleteMyDataText.setOnClickListener {
            val dialog = DeleteAccountDialogFragment(onConfirm = { deleteAccount() })
            dialog.show(childFragmentManager, "DeleteAccountDialog")
        }
    }

    private fun logout() {
        // 코루틴을 사용하여 API 호출
        lifecycleScope.launch {
            try {
                // SharedPreferences 등에서 저장된 토큰 가져오기 (이 부분은 실제 토큰 관리 로직에 맞게 수정 필요)
                val token = "Bearer YOUR_TOKEN" // "YOUR_TOKEN"을 실제 토큰으로 교체

                val response = apiService.logout(token)

                if (response.isSuccessful) {
                    // 로그아웃 성공 시 로그인 화면으로 이동
                    navigateToLoginScreen()
                } else {
                    // API 호출은 성공했으나, 서버에서 에러 응답을 보냈을 때
                    Toast.makeText(requireContext(), "로그아웃에 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 API 호출 자체가 실패했을 때
                Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 계정 탈퇴 API 호출 함수 (추가)
    private fun deleteAccount() {
        lifecycleScope.launch {
            try {
                // TODO: SharedPreferences 등에서 실제 저장된 토큰을 가져와야 합니다.
                val token = "Bearer YOUR_TOKEN" // "YOUR_TOKEN"을 실제 토큰으로 교체

                // 요청 본문 생성
                val requestBody = WithdrawRequest(agree = true)
                val response = apiService.withdraw(token, requestBody)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "계정 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    navigateToLoginScreen() // 성공 시 로그인 화면으로 이동
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("DeleteAccountError", "Code: $errorCode, Body: $errorBody")
                    Toast.makeText(requireContext(), "계정 탈퇴에 실패했습니다. (코드: $errorCode)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DeleteAccountException", "Error: ${e.message}", e)
                Toast.makeText(requireContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLoginScreen() {
        // Intent를 사용하여 LoginActivity 시작
        val intent = Intent(requireContext(), LoginActivity::class.java)
        // 기존의 모든 액티비티 스택을 제거하고 새로운 태스크로 시작
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}