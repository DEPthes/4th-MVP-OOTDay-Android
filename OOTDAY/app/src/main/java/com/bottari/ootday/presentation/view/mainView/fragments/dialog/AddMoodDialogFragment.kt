// presentation/view/mainView/fragments/dialog/AddMoodDialogFragment.kt

package com.bottari.ootday.presentation.view.mainView.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.bottari.ootday.databinding.DialogAddMoodBinding
import java.util.regex.Pattern

class AddMoodDialogFragment(
    private val onConfirm: (String) -> Unit, // 추가 버튼 클릭 시 실행될 함수
) : DialogFragment() {
    private var _binding: DialogAddMoodBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogAddMoodBinding.inflate(inflater, container, false)
        // 다이얼로그의 기본 배경을 투명하게 만들어 커스텀 배경이 잘 보이게 함
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기에는 추가 버튼을 비활성화합니다.
        binding.btnAdd.isEnabled = false

        // 2. EditText에 TextWatcher를 추가하여 실시간으로 입력을 감지합니다.
        binding.plusEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                // 3. 입력된 텍스트가 비어있지 않고, 오직 한글로만 구성되었는지 확인합니다.
                binding.btnAdd.isEnabled = inputText.isNotEmpty() && isKorean(inputText)
            }
        })

        // '추가' 버튼 리스너
        binding.btnAdd.setOnClickListener {
            val newKeyword = binding.plusEditText.text.toString().trim()
            onConfirm(newKeyword)
            dismiss()
        }

        // '취소' 버튼 리스너
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun isKorean(text: String): Boolean {
        // 정규표현식: 초성, 중성, 종성을 포함한 모든 한글 글자
        val pattern = Pattern.compile("^[가-힣]*$")
        return pattern.matcher(text).matches()
    }



    override fun onResume() {
        super.onResume()

        // sp 단위를 px로 변환하는 로직
        val widthInDp = 270f
        val widthInPx =
            TypedValue
                .applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    widthInDp,
                    resources.displayMetrics,
                ).toInt()

        // ✨ 다이얼로그의 너비를 270dp, 높이를 WRAP_CONTENT로 설정
        dialog?.window?.setLayout(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
