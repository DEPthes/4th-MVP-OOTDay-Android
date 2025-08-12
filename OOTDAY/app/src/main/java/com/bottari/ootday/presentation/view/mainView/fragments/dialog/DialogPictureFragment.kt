package com.bottari.ootday.presentation.view.mainView.fragments.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bottari.ootday.R
import com.bottari.ootday.databinding.DialogPictureBinding

class DialogPictureFragment(
    private val onCameraButtonClick: () -> Unit,
    private val onGalleryButtonClick: () -> Unit,
) : DialogFragment() {
    private var _binding: DialogPictureBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그의 배경을 투명하게 설정하여 둥근 모서리만 보이게 합니다.
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.useCamera.setOnClickListener {
            onCameraButtonClick()
            dismiss() // 다이얼로그 닫기
        }

        binding.useGallery.setOnClickListener {
            onGalleryButtonClick()
            dismiss() // 다이얼로그 닫기
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val params: WindowManager.LayoutParams = this.attributes
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.BOTTOM

            // ✅ XML에 있는 layout_marginBottom 값을 직접 가져와서 적용합니다.
            params.verticalMargin = resources.getDimensionPixelSize(R.dimen.margin_bottom) / resources.displayMetrics.heightPixels.toFloat()
            this.attributes = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
