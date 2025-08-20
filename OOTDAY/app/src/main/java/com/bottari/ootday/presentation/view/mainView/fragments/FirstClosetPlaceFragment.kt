package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.MoodPlaceViewModel
import com.bottari.ootday.databinding.FirstClosetPlaceFragmentBinding
import com.bottari.ootday.domain.model.KeywordItem
import com.bottari.ootday.presentation.view.mainView.adapters.KeywordAdapter
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.AddPlaceDialogFragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class FirstClosetPlaceFragment : Fragment() {
    private var _binding: FirstClosetPlaceFragmentBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: MoodPlaceViewModel by activityViewModels()
    private lateinit var keywordAdapter: KeywordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FirstClosetPlaceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        sharedViewModel.loadPlaceKeywords() // ViewModel에 장소 키워드 로드 요청

        binding.finishPlaceButton.setOnClickListener {
            // 👇 [핵심 추가] 결과 화면으로 이동하기 전, 현재 선택된 장소를 장바구니에 확실히 저장합니다.
            sharedViewModel.selectedPlace.value?.let {
            }
            findNavController().navigate(R.id.action_firstClosetPlaceFragment_to_firstClosetResultFragment)
        }
    }

    private fun setupRecyclerView() {
        // ✨ 선택 로직이 정상적으로 ViewModel에 전달되도록 어댑터 초기화
        keywordAdapter = KeywordAdapter { selectedItem ->
            when (selectedItem) {
                is KeywordItem.AddButton -> showAddKeywordDialog()
                is KeywordItem.KeywordData -> sharedViewModel.onKeywordClicked(selectedItem)
            }
        }

        val flexboxLayoutManager =
            FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

        binding.placeRecyclerview.apply {
            layoutManager = flexboxLayoutManager
            adapter = keywordAdapter
        }
    }

    private fun observeViewModel() {
        sharedViewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            keywordAdapter.submitList(keywords)
        }

        sharedViewModel.selectedCountText.observe(viewLifecycleOwner) { countText ->
            binding.ootdPlaceMaxCount.text = countText
        }

        sharedViewModel.isFinishButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.finishPlaceButton.isEnabled = isEnabled
        }
    }

    private fun showAddKeywordDialog() {
        val dialog = AddPlaceDialogFragment { newKeyword ->
            sharedViewModel.addNewKeyword(newKeyword)
        }
        dialog.show(childFragmentManager, "addPlaceDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.placeRecyclerview.adapter = null
        _binding = null
    }
}
