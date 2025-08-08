package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.FirstClosetViewModel
import com.bottari.ootday.databinding.FirstClosetFragmentBinding
import com.bottari.ootday.domain.model.DisplayableClosetItem
import com.bottari.ootday.presentation.view.mainView.adapters.ClosetAdapter
import com.bottari.ootday.data.model.mainModel.FirstClosetViewModelFactory
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.presentation.view.mainView.adapters.CenterItemDecoration

class FirstClosetFragment : Fragment() {

    private lateinit var binding: FirstClosetFragmentBinding

    private val viewModel: FirstClosetViewModel by viewModels {
        FirstClosetViewModelFactory(ClosetRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FirstClosetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        updateCategorySelection("상의")
        viewModel.loadItemsByCategory("상의")
    }

    private fun setupRecyclerView() {
        val closetAdapter = ClosetAdapter { item ->
            when (item) {
                is DisplayableClosetItem.AddButton -> {
                    // '추가하기' 버튼 클릭 시
                    // TODO: 아이템 추가 기능 구현
                }
                is DisplayableClosetItem.ClosetData -> {
                    // 일반 아이템 클릭 시
                    viewModel.toggleItemSelection(item)
                }
            }
        }

        binding.closetRecyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = closetAdapter
            addItemDecoration(CenterItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_small)))
        }
    }

    private fun setupListeners() {
        binding.closetTooltip.setOnClickListener {
            viewModel.onTooltipClicked()
        }

        binding.categoryTop.setOnClickListener {
            updateCategorySelection("상의")
            viewModel.loadItemsByCategory("상의")
        }
        binding.categoryBottom.setOnClickListener {
            updateCategorySelection("하의")
            viewModel.loadItemsByCategory("하의")
        }
        binding.categoryDress.setOnClickListener {
            updateCategorySelection("원피스")
            viewModel.loadItemsByCategory("원피스")
        }
        binding.categoryShoes.setOnClickListener {
            updateCategorySelection("신발")
            viewModel.loadItemsByCategory("신발")
        }
        binding.categoryPassionItem.setOnClickListener {
            updateCategorySelection("패션소품")
            viewModel.loadItemsByCategory("패션소품")
        }
        binding.categoryDecorations.setOnClickListener {
            updateCategorySelection("악세서리")
            viewModel.loadItemsByCategory("악세서리")
        }

        binding.stylingStartButton.setOnClickListener {
            // TODO: 다음 프래그먼트로 이동
        }
    }

    private fun updateCategorySelection(selectedCategory: String) {
        val gray100 = ContextCompat.getColor(requireContext(), R.color.gray_100)
        val gray200 = ContextCompat.getColor(requireContext(), R.color.gray_200)

        binding.categoryTop.setTextColor(if (selectedCategory == "상의") gray100 else gray200)
        binding.categoryBottom.setTextColor(if (selectedCategory == "하의") gray100 else gray200)
        binding.categoryDress.setTextColor(if (selectedCategory == "원피스") gray100 else gray200)
        binding.categoryShoes.setTextColor(if (selectedCategory == "신발") gray100 else gray200)
        binding.categoryPassionItem.setTextColor(if (selectedCategory == "패션소품") gray100 else gray200)
        binding.categoryDecorations.setTextColor(if (selectedCategory == "악세서리") gray100 else gray200)

        // ViewModel에도 선택 상태를 업데이트합니다.
        viewModel.onCategorySelected(selectedCategory, true)
    }

    private fun setupObservers() {
        viewModel.closetItems.observe(viewLifecycleOwner) { items ->
            (binding.closetRecyclerview.adapter as? ClosetAdapter)?.submitList(items)
        }

        viewModel.isTooltipVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.closetTooltip.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        viewModel.stylingButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.stylingStartButton.isEnabled = isEnabled
        }
    }
}