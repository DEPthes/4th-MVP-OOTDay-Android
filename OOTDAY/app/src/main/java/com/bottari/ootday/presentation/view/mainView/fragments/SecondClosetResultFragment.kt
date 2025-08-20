package com.bottari.ootday.presentation.view.mainView.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bottari.ootday.R // R 클래스 import
import com.bottari.ootday.databinding.SecondClosetResultFragmentBinding
import com.bumptech.glide.Glide

class SecondClosetResultFragment : Fragment() {
    private var _binding: SecondClosetResultFragmentBinding? = null
    val binding get() = _binding!!

    // Navigation Component를 통해 전달받은 arguments
    private val args: SecondClosetResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SecondClosetResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 추천 아이템 데이터
        val recommendedItem = args.recommendedItem

        // Glide를 이용해 추천 아이템 이미지 로드
        Glide
            .with(this)
            .load(recommendedItem.imageUrl)
            .placeholder(R.drawable.ic_loading_second) // 로딩 중 보여줄 이미지
            .into(binding.resultPicture)

        // '아이템 상세보기' 컨테이너 클릭 시
        binding.linkContainer.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recommendedItem.purchaseUrl))
                startActivity(intent)
            } catch (e: Exception) {
                // URL이 잘못되었거나, 처리할 앱이 없을 경우의 예외 처리
            }
        }

        // '홈으로 돌아가기' 버튼 클릭 시
        binding.backHomeButton.setOnClickListener {
            val startDestinationId = findNavController().graph.startDestinationId
            val navOptions =
                NavOptions
                    .Builder()
                    .setPopUpTo(startDestinationId, true)
                    .build()
            findNavController().navigate(startDestinationId, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
