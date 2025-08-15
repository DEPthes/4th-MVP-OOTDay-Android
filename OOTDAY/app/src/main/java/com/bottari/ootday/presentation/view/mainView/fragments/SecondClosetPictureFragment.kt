package com.bottari.ootday.presentation.view.mainView.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModel
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModelFactory
import com.bottari.ootday.databinding.SecondClosetPictureFragmentBinding
import com.bumptech.glide.Glide

class SecondClosetPictureFragment : Fragment() {
    private var _binding: SecondClosetPictureFragmentBinding? = null
    val binding get() = _binding!!

    private val args: SecondClosetPictureFragmentArgs by navArgs()

    // ✨ activityViewModels로 ViewModel을 초기화하여 Fragment간 공유
    private val viewModel: SecondClosetViewModel by activityViewModels {
        SecondClosetViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SecondClosetPictureFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = Uri.parse(args.imageUri)

        Glide
            .with(this)
            .load(imageUri)
            .into(binding.myPicture)

        binding.findMyItemButton.setOnClickListener {
            // ✨ 1. ViewModel에 가상 데이터 요청
            viewModel.fetchRecommendation()

            // ✨ 2. 로딩 화면으로 이동
            val action = SecondClosetPictureFragmentDirections.actionSecondClosetPictureFragmentToSecondClosetLoadingFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
