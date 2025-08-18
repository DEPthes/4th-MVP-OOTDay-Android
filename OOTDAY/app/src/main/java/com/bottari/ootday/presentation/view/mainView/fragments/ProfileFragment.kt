package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // activityViewModels import
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.R
import com.bottari.ootday.data.model.profileModel.ProfileViewModel
import com.bottari.ootday.databinding.ProfileFragmentBinding

class ProfileFragment : Fragment() {
    private var _binding: ProfileFragmentBinding? = null
    val binding get() = _binding!!

    // Activity 범위의 ViewModel을 가져와서 다른 Fragment와 공유
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()

        // 화면이 생성될 때 프로필 정보를 요청 (최초 1회)
        profileViewModel.fetchUserProfile()
    }

    private fun observeViewModel() {
        // userProfile LiveData를 관찰하여 UI 업데이트
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.nameText.text = profile.name
        }

        // 에러 메시지 LiveData 관찰
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 로딩 상태 LiveData 관찰 (예: ProgressBar 표시/숨김)
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // if (isLoading) binding.progressBar.visibility = View.VISIBLE
            // else binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.profileSettingButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_profileSettingFragment)
        }

        binding.myClosetButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_profileClosetFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
