// presentation/view/mainView/fragments/dialog/AddMoodDialogFragment.kt

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
import com.bottari.ootday.databinding.DialogAddMoodBinding

class AddMoodDialogFragment(
    private val onConfirm: (String) -> Unit // 추가 버튼 클릭 시 실행될 함수
) : DialogFragment() {

    private var _binding: DialogAddMoodBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddMoodBinding.inflate(inflater, container, false)
        // 다이얼로그의 기본 배경을 투명하게 만들어 커스텀 배경이 잘 보이게 함
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // '추가' 버튼 리스너
        binding.btnAdd.setOnClickListener {
            val newKeyword = binding.plusEditText.text.toString().trim()
            if (newKeyword.isNotEmpty()) {
                onConfirm(newKeyword) // Fragment로 텍스트 전달
                dismiss() // 다이얼로그 닫기
            }
        }

        // '취소' 버튼 리스너
        binding.btnCancel.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }

    override fun onResume() {
        super.onResume()

        // sp 단위를 px로 변환하는 로직
        val widthInDp = 270f
        val widthInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            widthInDp,
            resources.displayMetrics
        ).toInt()

        // ✨ 다이얼로그의 너비를 270dp, 높이를 WRAP_CONTENT로 설정
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}