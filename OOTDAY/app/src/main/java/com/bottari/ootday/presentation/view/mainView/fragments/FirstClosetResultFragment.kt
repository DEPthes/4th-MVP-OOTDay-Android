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

class FirstClosetResultFragment : Fragment() {
    private var _binding: FirstClosetResultFragmentBinding? = null
    val binding get() = _binding!!

    private var downloadCount = true

    private val resultViewModel: ClosetResultViewModel by viewModels {
        ClosetResultViewModelFactory(ClosetRepository(requireContext()))
    }

    private val sharedViewModel: MoodPlaceViewModel by navGraphViewModels(R.id.nav_graph)

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

        // 공유 ViewModel에 최종 코디 결과를 요청
        sharedViewModel.requestStyling()
    }

    private fun observeSharedViewModel() {
        // 공유 ViewModel이 서버로부터 결과 URL을 받아오면
        sharedViewModel.stylingResultUrls.observe(viewLifecycleOwner) { urls ->
            // 결과 ViewModel에 URL 목록을 전달하여 화면에 표시하도록 함
            resultViewModel.setImageUrls(urls) // 👈 setImageUrls 함수는 직접 추가해야 함
        }

        sharedViewModel.isLoadingResult.observe(viewLifecycleOwner) { isLoading ->
            // 로딩 UI 처리
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
