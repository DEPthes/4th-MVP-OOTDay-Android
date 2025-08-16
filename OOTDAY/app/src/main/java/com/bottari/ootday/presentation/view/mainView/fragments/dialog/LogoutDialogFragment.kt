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
import com.bottari.ootday.databinding.DialogLogoutBinding

class LogoutDialogFragment(
    private val onConfirm: () -> Unit, // '로그아웃' 버튼 클릭 시 실행될 함수
) : DialogFragment() {

    private var _binding: DialogLogoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogLogoutBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // '로그아웃' 버튼 리스너 (btn_add는 확인 버튼으로 사용)
        binding.btnAdd.setOnClickListener {
            onConfirm() // ProfileSettingFragment로 콜백 실행
            dismiss()   // 다이얼로그 닫기
        }

        // '취소' 버튼 리스너
        binding.btnCancel.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }
    }

    override fun onResume() {
        super.onResume()
        // 다이얼로그 크기 설정
        val widthInDp = 270f
        val widthInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            widthInDp,
            resources.displayMetrics
        ).toInt()
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}