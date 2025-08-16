package com.bottari.ootday.presentation.view.mainView.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bottari.ootday.R
import com.bottari.ootday.databinding.DialogDeleteBinding

class DeleteAccountDialogFragment(
    private val onConfirm: () -> Unit, // '탈퇴' 버튼 클릭 시 실행될 함수
) : DialogFragment() {

    private var _binding: DialogDeleteBinding? = null
    private val binding get() = _binding!!

    private var isAgreed = false // 동의 상태를 저장할 변수

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeleteBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 상태 설정: 탈퇴 버튼 비활성화
        binding.btnAdd.isEnabled = false

        // 2. '유의사항 동의' LinearLayout 클릭 리스너
        binding.allAgreeButton.setOnClickListener {
            isAgreed = !isAgreed // 동의 상태를 반전
            updateUiForAgreement() // UI 상태 업데이트
        }

        // 3. '탈퇴' 버튼 리스너 (btn_add)
        binding.btnAdd.setOnClickListener {
            onConfirm()
            dismiss()
        }

        // 4. '취소' 버튼 리스너
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    // 동의 상태에 따라 UI를 업데이트하는 함수
    private fun updateUiForAgreement() {
        binding.agreeCheckbox.isChecked = isAgreed
        binding.btnAdd.isEnabled = isAgreed

        val textColorResId = if (isAgreed) R.color.gray_100 else R.color.gray_200
        binding.agreeText.setTextColor(ContextCompat.getColor(requireContext(), textColorResId))
    }

    override fun onResume() {
        super.onResume()
        // 다이얼로그 크기 설정
        val widthInDp = 270f
        val widthInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, widthInDp, resources.displayMetrics
        ).toInt()
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}