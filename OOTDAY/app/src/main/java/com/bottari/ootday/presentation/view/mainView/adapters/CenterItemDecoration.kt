package com.bottari.ootday.presentation.view.mainView.adapters

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CenterItemDecoration(
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        // 모든 아이템의 좌우에 동일한 간격을 적용하여 중앙 정렬 효과를 냅니다.
        outRect.left = spacing
        outRect.right = spacing
    }
}
