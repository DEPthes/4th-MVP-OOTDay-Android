package com.bottari.ootday.presentation.view.mainView.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bottari.ootday.data.model.profileModel.ProfileViewModel
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

    private val profileViewModel: ProfileViewModel by activityViewModels()

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
        observeViewModel()
    }

    private fun observeViewModel() {
        // 여기서도 userProfile LiveData를 관찰하여 UI를 업데이트
        // API 호출은 ProfileFragment에서 이미 했으므로, 여기서는 데이터만 받아서 사용
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.nameText.text = profile.name // 상단 이름
            binding.secretNameText.text = profile.name // 개인정보 카드 - 이름
            binding.secretIdText.text = profile.memberId // 개인정보 카드 - 아이디
        }

        // 에러 메시지 관찰
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 화면 이동 이벤트 관찰 (추가)
        profileViewModel.navigateToLogin.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToLoginScreen()
            }
        }
    }

    private fun setupClickListeners() {
        /// 로그아웃 클릭 시 ViewModel의 logout 함수 호출
        binding.logoutText.setOnClickListener {
            val dialog = LogoutDialogFragment(onConfirm = { profileViewModel.logout() })
            dialog.show(childFragmentManager, "LogoutDialog")
        }

        // 계정 탈퇴 클릭 시 ViewModel의 deleteAccount 함수 호출
        binding.deleteMyDataText.setOnClickListener {
            val dialog =
                DeleteAccountDialogFragment(onConfirm = { profileViewModel.deleteAccount() })
            dialog.show(childFragmentManager, "DeleteAccountDialog")
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