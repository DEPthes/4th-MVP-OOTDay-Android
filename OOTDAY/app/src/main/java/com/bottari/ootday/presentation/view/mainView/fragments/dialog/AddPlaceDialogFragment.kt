package com.bottari.ootday.presentation.view.mainView.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.bottari.ootday.databinding.DialogAddPlaceBinding

class AddPlaceDialogFragment(
    private val onAddButtonClick: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddPlaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPlaceBinding.inflate(inflater, container, false)
        // ✨ 다이얼로그 창의 기본 타이틀과 배경을 제거하여 커스텀 디자인이 잘 보이게 함
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAdd.setOnClickListener {
            val newKeyword = binding.plusEditText.text.toString().trim()
            if (newKeyword.isNotEmpty()) {
                onAddButtonClick(newKeyword)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        // sp 단위를 px로 변환하는 로직
        val widthInSp = 270f
        val widthInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            widthInSp,
            resources.displayMetrics
        ).toInt()
        // 다이얼로그의 너비를 270sp에 해당하는 px 값으로, 높이를 WRAP_CONTENT로 설정
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}