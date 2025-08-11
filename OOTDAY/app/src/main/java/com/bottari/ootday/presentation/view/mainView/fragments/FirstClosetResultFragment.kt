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
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.ClosetResultViewModelFactory
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.databinding.FirstClosetResultFragmentBinding
import com.bottari.ootday.presentation.viewmodel.ClosetResultViewModel
import com.bumptech.glide.Glide

class FirstClosetResultFragment : Fragment() {

    private var _binding: FirstClosetResultFragmentBinding? = null
    private val binding get() = _binding!!
    private var downloadCount = true

    private val viewModel: ClosetResultViewModel by viewModels {
        ClosetResultViewModelFactory(ClosetRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FirstClosetResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadCombination()
        observeViewModel()
        setupClickListeners() // ✨ 클릭 리스너 설정 함수 호출
    }

    private fun observeViewModel() {
        viewModel.imageUrls.observe(viewLifecycleOwner) { urls ->
            val imageViews = listOf(
                binding.resultImage1, binding.resultImage2, binding.resultImage3,
                binding.resultImage4, binding.resultImage5, binding.resultImage6
            )

            // 받아온 URL 개수만큼 이미지를 순서대로 넣어줍니다.
            urls.forEachIndexed { index, url ->
                if (index < imageViews.size) {
                    loadImageWithGlide(imageViews[index], url)
                }
            }
        }

        viewModel.downloadStatus.observe(viewLifecycleOwner) { statusMessage ->
            Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show()
        }

    }

    // ✨ 클릭 리스너들을 모아두는 함수
    private fun setupClickListeners() {
        // ✨ 전체 이미지 다운로드 버튼 리스너
        binding.downloadContainer.setOnClickListener {
            if(downloadCount == true) {
                // ViewModel에 다운로드 요청
                viewModel.downloadImages(requireContext())

                // 한 번 누르면 버튼 비활성화
                binding.downloadIcon.isEnabled = false

                // 텍스트 색상 변경
                binding.downloadText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_200))
                binding.downloadText.alpha = 1.0f
                downloadCount = false
            }

        }

        // ✨ 홈으로 돌아가기 버튼 리스너
        binding.backHome.setOnClickListener {
            // 스택에 쌓인 모든 Fragment를 제거하고 시작 화면(홈)으로 이동
            val startDestinationId = findNavController().graph.startDestinationId
            val navOptions = NavOptions.Builder()
                .setPopUpTo(startDestinationId, true)
                .build()
            findNavController().navigate(startDestinationId, null, navOptions)
        }
    }

    // ... loadImageWithGlide 함수는 동일 ...
    private fun loadImageWithGlide(imageView: ImageView, url: String) {
        Glide.with(this)
            .load(url)
            .centerCrop()
            .into(imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}