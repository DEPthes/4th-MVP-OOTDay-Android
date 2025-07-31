package com.bottari.ootday.presentation.view.signupView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bottari.ootday.data.model.signupModel.SignUpStep2ViewModel
import com.bottari.ootday.databinding.SignUpStep2Binding
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity

class SignUpStep2Fragment : Fragment() {
    private var _binding: SignUpStep2Binding? = null
    val binding get() = _binding!!

    // ViewModel 인스턴스 생성 (MVVM 패턴)
    private val viewModel: SignUpStep2ViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SignUpStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // UI 요소 초기화 및 ViewModel 관찰
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // 예시: step1_login_button (이름 입력 후 다음 버튼) 클릭 시
        // XML 파일에 따라 Button ID가 다를 수 있으니 확인해주세요.
        binding.step2NextButton.setOnClickListener {
            // `sign_up_step1.xml`의 `login_button` ID 사용
            // ViewModel에 이벤트 전달 또는 유효성 검사 로직 호출
            // 예: viewModel.onNextButtonClicked(binding.step1LoginArea.text.toString())
            (activity as? SignUpActivity)?.navigateToNextStep(2) // 다음 단계로 이동 (SignUpActivity에 위임)
        }
    }

    private fun observeViewModel() {
        // ViewModel의 LiveData를 관찰하여 UI 업데이트 (MVVM 패턴의 핵심)
        // 예: viewModel.isValidInput.observe(viewLifecycleOwner) { isValid ->
        //     binding.loginButton.isEnabled = isValid
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
