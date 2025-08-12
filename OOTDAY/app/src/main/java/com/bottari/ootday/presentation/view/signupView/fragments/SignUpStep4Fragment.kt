package com.bottari.ootday.presentation.view.signupView.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.signupModel.SignUpStep4ViewModel
import com.bottari.ootday.data.model.signupModel.SignUpViewModel
import com.bottari.ootday.databinding.SignUpStep4Binding
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity

class SignUpStep4Fragment : Fragment() {
    private var _binding: SignUpStep4Binding? = null
    val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by activityViewModels() // 추가

    private val viewModel: SignUpStep4ViewModel by viewModels()

    private var isPasswordVisible = false
    private var isConfirmVisible = false

    // Fragment에서 Context를 사용해 폰트를 로드할 변수를 추가합니다.
    private lateinit var notoSansKrRegular: Typeface

    private val passwordTextWatcher =
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
                viewModel.onPasswordInputChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    private val passwordConfirmTextWatcher =
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
                viewModel.onPasswordConfirmChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SignUpStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            notoSansKrRegular = ResourcesCompat.getFont(it, R.font.noto_sans_kr_regular)!!
        }

        setupListeners()
        observeViewModel()

        viewModel.onPasswordInputChanged(binding.step4PasswordInput.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.step4PasswordInput.removeTextChangedListener(passwordTextWatcher)
        binding.step4PasswordConfirmInput.removeTextChangedListener(passwordConfirmTextWatcher)
        _binding = null
    }

    private fun setupListeners() {
        binding.step4PasswordInput.addTextChangedListener(passwordTextWatcher)
        binding.step4PasswordConfirmInput.addTextChangedListener(passwordConfirmTextWatcher)

        binding.step4PasswordInputToggleButton.setOnClickListener {
            togglePasswordVisibility(binding.step4PasswordInput, binding.step4PasswordInputToggleButton)
        }

        binding.step4PasswordConfirmToggleButton.setOnClickListener {
            toggleConfirmPasswordVisibility(binding.step4PasswordConfirmInput, binding.step4PasswordConfirmToggleButton)
        }

        binding.step4NextButton.setOnClickListener {
            val canProceed = viewModel.onNextButtonClicked()
            if (canProceed) {
                // ViewModel에 비밀번호 데이터 저장
                val password = binding.step4PasswordInput.text.toString()
                signUpViewModel.setPassword(password)

                (activity as? SignUpActivity)?.navigateToNextStep(4)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step4NextButton.isEnabled = isEnabled
        }

        viewModel.isConfirmVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) {
                binding.step4PasswordConfirmInput.visibility = View.VISIBLE
                binding.step4PasswordConfirmToggleButton.visibility = View.VISIBLE
                // 에러가 있을 때만 보이도록 GONE으로 초기화
                binding.step4PsConfirmError.visibility = View.GONE
            } else {
                binding.step4PasswordConfirmInput.visibility = View.GONE
                binding.step4PasswordConfirmToggleButton.visibility = View.GONE
                binding.step4PsConfirmError.visibility = View.GONE
            }
        }

        viewModel.passwordError.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.step4PsInputError.text = errorMessage
                binding.step4PsInputError.visibility = View.VISIBLE
                setEditTextUnderlineColor(binding.step4PasswordInput, R.color.font_red)
            } else {
                binding.step4PsInputError.visibility = View.GONE
                setEditTextUnderlineColor(binding.step4PasswordInput, R.color.gray_100)
            }
        }

        viewModel.passwordConfirmError.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.step4PsConfirmError.text = errorMessage
                binding.step4PsConfirmError.visibility = View.VISIBLE
                setEditTextUnderlineColor(binding.step4PasswordConfirmInput, R.color.font_red)
            } else {
                binding.step4PsConfirmError.visibility = View.GONE
                setEditTextUnderlineColor(binding.step4PasswordConfirmInput, R.color.gray_100)
            }
        }
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        toggleButton: ImageButton,
    ) {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.btn_ps_visible_on)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.btn_ps_visible_off)
        }
        // 토글 시 커서 위치를 유지하기 위해 추가
        editText.setSelection(editText.text.length)

        // **여기에 폰트 재설정 코드를 추가합니다.**
        editText.typeface = notoSansKrRegular
    }

    private fun toggleConfirmPasswordVisibility(
        editText: EditText,
        toggleButton: ImageButton,
    ) {
        isConfirmVisible = !isConfirmVisible

        if (isConfirmVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.btn_ps_visible_on)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.btn_ps_visible_off)
        }
        editText.setSelection(editText.text.length)

        // **여기에 폰트 재설정 코드를 추가합니다.**
        editText.typeface = notoSansKrRegular
    }

    private fun setEditTextUnderlineColor(
        editText: EditText,
        colorResId: Int,
    ) {
        val color = ContextCompat.getColor(requireContext(), colorResId)
        val drawable = editText.background
        if (drawable != null) {
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            editText.background = wrappedDrawable
        }
    }
}
