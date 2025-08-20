package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModel
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModelFactory
import com.bottari.ootday.databinding.SecondClosetLoadingFragmentBinding

class SecondClosetLoadingFragment : Fragment() {
    private var _binding: SecondClosetLoadingFragmentBinding? = null
    val binding get() = _binding!!

    private val viewModel: SecondClosetViewModel by activityViewModels {
        SecondClosetViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SecondClosetLoadingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel의 추천 결과 LiveData를 관찰
        viewModel.recommendationResult.observe(viewLifecycleOwner) { result ->
            // result가 null이 아닐 때만 실행
            result?.let { recommendedItem ->
                // 결과 데이터를 담아 Result Fragment로 이동
                val action =
                    SecondClosetLoadingFragmentDirections.actionSecondClosetLoadingFragmentToSecondClosetResultFragment(
                        recommendedItem,
                    )
                findNavController().navigate(action)

                // LiveData 초기화 (중복 실행 방지)
                viewModel.onRecommendationShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
