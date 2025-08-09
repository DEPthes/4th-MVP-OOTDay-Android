package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bottari.ootday.R

class FirstClosetMoodFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // FirstClosetMoodFragment의 레이아웃 파일을 인플레이트합니다.
        // 아직 레이아웃 파일이 없다면, `res/layout` 폴더에 `first_closet_mood_fragment.xml` 파일을 만들어주세요.
        return inflater.inflate(R.layout.first_closet_mood_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 여기에서 뷰와 상호작용하는 로직을 작성합니다.
    }
}