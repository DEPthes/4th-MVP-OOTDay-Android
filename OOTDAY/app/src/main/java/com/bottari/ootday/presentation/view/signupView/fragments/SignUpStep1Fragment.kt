package com.bottari.ootday.presentation.view.signupView.fragments // 올바른 패키지명

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.signupModel.SignUpStep1ViewModel
import com.bottari.ootday.databinding.SignUpStep1Binding
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity
import androidx.fragment.app.activityViewModels // 추가
import com.bottari.ootday.data.model.signupModel.SignUpViewModel // 추가


class SignUpStep1Fragment : Fragment() {

    private var _binding: SignUpStep1Binding? = null
    val binding get() = _binding!! // ktlint 규칙 준수를 위해 'private' 제거

    // SignUpActivity의 ViewModel을 공유
    private val signUpViewModel: SignUpViewModel by activityViewModels()

    private val viewModel: SignUpStep1ViewModel by viewModels()

    // EditText 텍스트 변경을 ViewModel에 알리는 TextWatcher
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // 텍스트 변경 이벤트를 ViewModel로 전달
            viewModel.onInputNameChanged(s.toString())
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignUpStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        // Fragment가 처음 생성될 때, 이미 EditText에 텍스트가 있다면 초기 유효성 검사 실행
        viewModel.onInputNameChanged(binding.step1LoginArea.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지를 위해 TextWatcher 해제
        binding.step1LoginArea.removeTextChangedListener(textWatcher)
        _binding = null
    }

    private fun setupListeners() {
        // EditText에 TextWatcher 추가
        binding.step1LoginArea.addTextChangedListener(textWatcher)

        // '다음' 버튼 클릭 리스너
        binding.step1NextButton.setOnClickListener {
            // ViewModel에 최종 유효성 검사를 요청하고 결과를 받음
            val canProceed = viewModel.onNextButtonClicked()

            if (canProceed) {
                // 모든 유효성 검사 통과 시 다음 단계로 이동
                val name = binding.step1LoginArea.text.toString()
                signUpViewModel.setName(name)
                (activity as? SignUpActivity)?.navigateToNextStep(2) // 현재 단계 번호 1 전달
            }
            // else: ViewModel에서 displayErrorMessage LiveData를 통해 오류 메시지가 이미 설정되었으므로 별도 처리 불필요
        }
    }

    private fun observeViewModel() {
        // ViewModel의 'isNextButtonEnabled' LiveData를 관찰하여 버튼 활성화/비활성화 상태 업데이트
        viewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step1NextButton.isEnabled = isEnabled
        }

        // ViewModel의 'displayErrorMessage' LiveData를 관찰하여 오류 메시지 표시/숨김 및 EditText 밑줄 색상 변경
        viewModel.displayErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.step1NameError.text = errorMessage
                binding.step1NameError.visibility = View.VISIBLE
                setEditTextUnderlineColor(binding.step1LoginArea, R.color.font_red)
            } else {
                binding.step1NameError.visibility = View.GONE
                setEditTextUnderlineColor(binding.step1LoginArea, R.color.gray_100)
            }
        }
    }

    /**
     * EditText의 밑줄 색상을 변경하는 유틸리티 함수입니다.
     * @param editText 색상을 변경할 EditText 인스턴스
     * @param colorResId 적용할 색상의 리소스 ID (예: R.color.gray_dark)
     */
    private fun setEditTextUnderlineColor(editText: android.widget.EditText, colorResId: Int) {
        val color = ContextCompat.getColor(requireContext(), colorResId)
        val drawable = editText.background
        if (drawable != null) {
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            editText.background = wrappedDrawable
        }
    }
}