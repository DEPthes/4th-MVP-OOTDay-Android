package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.ClosetResultViewModelFactory
import com.bottari.ootday.data.model.mainModel.MoodPlaceViewModel
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.databinding.FirstClosetResultFragmentBinding
import com.bottari.ootday.presentation.viewmodel.ClosetResultViewModel
import com.bumptech.glide.Glide
import androidx.fragment.app.activityViewModels

class FirstClosetResultFragment : Fragment() {
    private var _binding: FirstClosetResultFragmentBinding? = null
    val binding get() = _binding!!

    private var downloadCount = true

    private val resultViewModel: ClosetResultViewModel by viewModels {
        ClosetResultViewModelFactory(ClosetRepository(requireContext()))
    }

    private val sharedViewModel: MoodPlaceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FirstClosetResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeSharedViewModel() // 공유 ViewModel 관찰 시작
        observeResultViewModel() // 결과 ViewModel 관찰 시작
        setupClickListeners()

        // 화면이 생성될 때 딱 한 번만 스타일링을 요청
        // (화면 회전 등 재생성 시 중복 요청 방지)
        if (sharedViewModel.stylingResultUrls.value == null) {
            sharedViewModel.requestStyling()
        }
    }

    private fun observeSharedViewModel() {
        sharedViewModel.stylingResultUrls.observe(viewLifecycleOwner) { urls ->
            if (urls.isNotEmpty()) {
                // 성공적으로 URL 목록을 받으면 결과 ViewModel에 전달
                resultViewModel.setImageUrls(urls)
            } else {
                // 빈 목록이 오면 (API 실패 등) 사용자에게 알림
                Toast.makeText(context, "코디 조합에 실패했습니다.", Toast.LENGTH_SHORT).show()
                // 필요하다면 이전 화면으로 돌아가는 로직 추가
            }
        }
    }


    private fun observeResultViewModel() {
        resultViewModel.imageUrls.observe(viewLifecycleOwner) { urls ->
            val imageViews = listOf(binding.resultImage1, binding.resultImage2, binding.resultImage3, binding.resultImage4, binding.resultImage5, binding.resultImage6)
            imageViews.forEach { it.setImageDrawable(null) } // 이미지 초기화
            urls.forEachIndexed { index, url ->
                if (index < imageViews.size) {
                    loadImageWithGlide(imageViews[index], url)
                }
            }

        }

        resultViewModel.downloadStatus.observe(viewLifecycleOwner) { statusMessage ->
            Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show()
        }

        // 다운로드 버튼 활성화 상태 관찰
        resultViewModel.isDownloadEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.downloadContainer.isEnabled = isEnabled
            val textColor = if (isEnabled) R.color.gray_100 else R.color.gray_200
            binding.downloadText.setTextColor(ContextCompat.getColor(requireContext(), textColor))
            binding.downloadIcon.isEnabled = isEnabled
        }
    }

    // ✨ 클릭 리스너들을 모아두는 함수
    private fun setupClickListeners() {
        binding.downloadContainer.setOnClickListener {
            resultViewModel.downloadImages(requireContext())
        }

        binding.backHome.setOnClickListener {
            val startDestinationId = findNavController().graph.startDestinationId
            val navOptions = NavOptions.Builder().setPopUpTo(startDestinationId, true).build()
            findNavController().navigate(startDestinationId, null, navOptions)
        }
    }

    // ... loadImageWithGlide 함수는 동일 ...
    private fun loadImageWithGlide(
        imageView: ImageView,
        url: String,
    ) {
        Glide
            .with(this)
            .load(url)
            .centerCrop()
            .into(imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
