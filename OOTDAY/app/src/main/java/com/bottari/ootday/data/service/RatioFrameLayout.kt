package com.bottari.ootday.data.service

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bottari.ootday.R

class RatioFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var ratioWidth = 0f
    private var ratioHeight = 0f

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.RatioFrameLayout)
            val ratioString = typedArray.getString(R.styleable.RatioFrameLayout_ratio)

            if (!ratioString.isNullOrEmpty()) {
                val parts = ratioString.split(":")
                if (parts.size == 2) {
                    ratioWidth = parts[0].toFloatOrNull() ?: 0f
                    ratioHeight = parts[1].toFloatOrNull() ?: 0f
                }
            }
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 부모로부터 받은 가로 길이를 기준으로 세로 길이를 비율에 맞게 강제로 재계산
        if (ratioWidth > 0 && ratioHeight > 0) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = (width * ratioHeight / ratioWidth).toInt()
            val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}