package com.bottari.ootday.presentation.view.signupView.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.signupModel.SignUpStep2ViewModel
import com.bottari.ootday.data.model.signupModel.SignUpViewModel
import com.bottari.ootday.databinding.SignUpStep2Binding
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity

class SignUpStep2Fragment : Fragment() {
    private var _binding: SignUpStep2Binding? = null
    val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by activityViewModels() // 추가

    private val viewModel: SignUpStep2ViewModel by viewModels()

    private val phoneNumberTextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                viewModel.onPhoneNumberChanged(s.toString())
                setEditTextUnderlineColor(binding.step2PhoneNumberInput, R.color.gray_100)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    private val authCodeTextWatcher =
        object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                viewModel.onAuthCodeChanged(s.toString())
                setEditTextUnderlineColor(binding.step2PhoneNumberConfirm, R.color.gray_100)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

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

        setupListeners()
        observeViewModel()
        initialUiState()
    }

    private fun setupListeners() {
        binding.step2PhoneNumberInput.addTextChangedListener(phoneNumberTextWatcher)
        binding.step2PhoneNumberConfirm.addTextChangedListener(authCodeTextWatcher)

        binding.step2GetAuthButton.setOnClickListener {
            viewModel.onRequestAuthClick()
        }

        binding.step2CheckAuthButton.setOnClickListener {
            viewModel.onCheckAuthClick()
        }

        binding.step2NextButton.setOnClickListener {
            val phoneNumber = binding.step2PhoneNumberInput.text.toString()
            signUpViewModel.setPhoneNumber(phoneNumber)

            (activity as? SignUpActivity)?.navigateToNextStep(1)
        }
    }

    private fun observeViewModel() {
        viewModel.isAuthSuccessful.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                // 인증 성공 시 휴대폰 번호 입력 EditText 비활성화
                binding.step2PhoneNumberInput.isEnabled = false
            }
        }

        // (기존 코드 유지)
        viewModel.isGetAuthButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step2GetAuthButton.isEnabled = isEnabled
        }

        viewModel.isCheckAuthButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step2CheckAuthButton.isEnabled = isEnabled
        }

        viewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step2NextButton.isEnabled = isEnabled
        }

        // 새롭게 추가된 LiveData 관찰 로직
        viewModel.isAuthCodeInputEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step2PhoneNumberConfirm.isEnabled = isEnabled // 인증번호 입력창 활성화
        }

        viewModel.authCodeErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.step2AuthNumberError.text = errorMessage
                binding.step2AuthNumberError.visibility = View.VISIBLE
                setEditTextUnderlineColor(binding.step2PhoneNumberConfirm, R.color.font_red)
            } else {
                binding.step2AuthNumberError.visibility = View.GONE
                setEditTextUnderlineColor(binding.step2PhoneNumberConfirm, R.color.gray_100)
            }
        }

        viewModel.timerText.observe(viewLifecycleOwner) { timerText ->
            if (timerText != null) {
                binding.step2TimerText.text = timerText
                binding.step2TimerText.visibility = View.VISIBLE
            } else {
                binding.step2TimerText.visibility = View.GONE
            }
        }

        viewModel.eventMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.step2GetAuthButton.isClickable = !isLoading
            binding.step2CheckAuthButton.isClickable = !isLoading
            binding.step2NextButton.isClickable = !isLoading
            binding.step2PhoneNumberInput.isClickable = !isLoading
            binding.step2PhoneNumberConfirm.isClickable = !isLoading
        }
    }

    private fun initialUiState() {
        // (기존 코드 유지)
        binding.step2GetAuthButton.isEnabled = false
        binding.step2CheckAuthButton.isEnabled = false
        binding.step2NextButton.isEnabled = false
        binding.step2PhoneNumberConfirm.isEnabled = false // 초기에는 비활성화 상태

        binding.step2TimerText.visibility = View.GONE
        binding.step2AuthNumberError.visibility = View.GONE

        setEditTextUnderlineColor(binding.step2PhoneNumberInput, R.color.gray_100)
        setEditTextUnderlineColor(binding.step2PhoneNumberConfirm, R.color.gray_100)
    }

    private fun setEditTextUnderlineColor(
        editText: EditText,
        colorResId: Int,
    ) {
        // (기존 코드 유지)
        val color = ContextCompat.getColor(requireContext(), colorResId)
        val drawable = editText.background
        if (drawable != null) {
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            editText.background = wrappedDrawable
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.step2PhoneNumberInput.removeTextChangedListener(phoneNumberTextWatcher)
        binding.step2PhoneNumberConfirm.removeTextChangedListener(authCodeTextWatcher)
        _binding = null
    }
}
