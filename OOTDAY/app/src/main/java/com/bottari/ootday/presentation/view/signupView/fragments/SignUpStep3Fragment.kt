package com.bottari.ootday.presentation.view.signupView.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.signupModel.SignUpStep3ViewModel
import com.bottari.ootday.data.model.signupModel.SignUpViewModel
import com.bottari.ootday.databinding.SignUpStep3Binding
import com.bottari.ootday.presentation.view.signupView.activities.SignUpActivity

class SignUpStep3Fragment : Fragment() {
    private var _binding: SignUpStep3Binding? = null
    val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by activityViewModels() // 추가

    private val viewModel: SignUpStep3ViewModel by viewModels()

    private val textWatcher =
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
                viewModel.onInputIdChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SignUpStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        viewModel.onInputIdChanged(binding.step3IdInput.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.step3IdInput.removeTextChangedListener(textWatcher)
        _binding = null
    }

    private fun setupListeners() {
        binding.step3IdInput.addTextChangedListener(textWatcher)

        binding.step3NextButton.setOnClickListener {
            val canProceed = viewModel.onNextButtonClicked()
            if (canProceed) {
                // ViewModel에 아이디 데이터 저장
                val id = binding.step3IdInput.text.toString()
                signUpViewModel.setId(id)

                (activity as? SignUpActivity)?.navigateToNextStep(3)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isNextButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.step3NextButton.isEnabled = isEnabled
        }

        viewModel.displayErrorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                binding.step3IdError.text = errorMessage
                binding.step3IdError.visibility = View.VISIBLE
                setEditTextUnderlineColor(binding.step3IdInput, R.color.font_red)
            } else {
                binding.step3IdError.visibility = View.GONE
                setEditTextUnderlineColor(binding.step3IdInput, R.color.gray_100)
            }
        }
    }

    private fun setEditTextUnderlineColor(
        editText: android.widget.EditText,
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
