package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.MoodPlaceViewModel
import com.bottari.ootday.databinding.FirstClosetMoodFragmentBinding
import com.bottari.ootday.domain.model.KeywordItem
import com.bottari.ootday.presentation.view.mainView.adapters.KeywordAdapter
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.AddMoodDialogFragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class FirstClosetMoodFragment : Fragment() {
    private var _binding: FirstClosetMoodFragmentBinding? = null
    val binding get() = _binding!!

    private val viewModel: MoodPlaceViewModel by viewModels()
    private lateinit var keywordAdapter: KeywordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FirstClosetMoodFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadMoodKeywords()

        binding.finishMoodButton.setOnClickListener {
            findNavController().navigate(R.id.action_firstClosetMoodFragment_to_firstClosetPlaceFragment)
        }
    }

    private fun setupRecyclerView() {
        keywordAdapter =
            KeywordAdapter { selectedItem ->
                when (selectedItem) {
                    is KeywordItem.AddButton -> showAddKeywordDialog()
                    is KeywordItem.KeywordData -> viewModel.onKeywordClicked(selectedItem)
                }
            }

        val flexboxLayoutManager =
            FlexboxLayoutManager(requireContext()).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

        binding.moodRecyclerview.apply {
            layoutManager = flexboxLayoutManager
            adapter = keywordAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.keywords.observe(viewLifecycleOwner) { keywords ->
            keywordAdapter.submitList(keywords)
        }

        viewModel.selectedCountText.observe(viewLifecycleOwner) { countText ->
            binding.ootdMoodMaxCount.text = countText
        }

        viewModel.isFinishButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.finishMoodButton.isEnabled = isEnabled
        }
    }

    private fun showAddKeywordDialog() {
        val dialog =
            AddMoodDialogFragment { newKeyword ->
                // ✨ 이 부분이 정상적으로 ViewModel의 addNewKeyword를 호출합니다.
                viewModel.addNewKeyword(newKeyword)
            }
        dialog.show(childFragmentManager, "AddMoodDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.moodRecyclerview.adapter = null
        _binding = null
    }
}
