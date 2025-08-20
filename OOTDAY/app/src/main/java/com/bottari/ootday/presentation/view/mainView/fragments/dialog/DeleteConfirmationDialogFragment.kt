package com.bottari.ootday.presentation.view.mainView.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bottari.ootday.databinding.DialogDeleteClothBinding

class DeleteConfirmationDialogFragment(
    // [핵심] '삭제' 버튼을 눌렀을 때 실행될 동작을 외부(Fragment)에서 전달받습니다.
    private val onDeleteConfirmed: () -> Unit
) : DialogFragment() {

    private var _binding: DialogDeleteClothBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeleteClothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 취소 버튼: 다이얼로그를 닫기만 함
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // 삭제 버튼: 외부에서 전달받은 동작(onDeleteConfirmed)을 실행하고 다이얼로그를 닫음
        binding.btnDeleteCloth.setOnClickListener {
            onDeleteConfirmed()
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        // sp 단위를 px로 변환하는 로직
        val widthInSp = 270f
        val widthInPx =
            TypedValue
                .applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    widthInSp,
                    resources.displayMetrics,
                ).toInt()
        // 다이얼로그의 너비를 270sp에 해당하는 px 값으로, 높이를 WRAP_CONTENT로 설정
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 배경을 투명하게 하고, XML에 정의된 Padding과 rounded corner를 적용
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}