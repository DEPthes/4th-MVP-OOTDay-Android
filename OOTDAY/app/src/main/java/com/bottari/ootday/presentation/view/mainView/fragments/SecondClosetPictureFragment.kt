package com.bottari.ootday.presentation.view.mainView.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModel
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModelFactory
import com.bottari.ootday.databinding.SecondClosetPictureFragmentBinding
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class SecondClosetPictureFragment : Fragment() {
    private var _binding: SecondClosetPictureFragmentBinding? = null
    val binding get() = _binding!!

    private val args: SecondClosetPictureFragmentArgs by navArgs()

    // ✨ activityViewModels로 ViewModel을 초기화하여 Fragment간 공유
    private val viewModel: SecondClosetViewModel by activityViewModels {
        SecondClosetViewModelFactory(requireContext())
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

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.findMyItemButton.setOnClickListener {
            // URI를 MultipartBody.Part로 변환
            val imagePart = uriToMultipartBodyPart(imageUri)
            if (imagePart != null) {
                // 1. ViewModel에 실제 데이터 요청
                viewModel.fetchRecommendation(imagePart)
                // 2. 로딩 화면으로 이동
                findNavController().navigate(R.id.action_secondClosetPictureFragment_to_secondClosetLoadingFragment)
            } else {
                Toast.makeText(requireContext(), "이미지 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToMultipartBodyPart(uri: Uri): MultipartBody.Part? =
        try {
            val fileContent = requireContext().contentResolver.openInputStream(uri)?.readBytes()
            fileContent?.let {
                val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", "image.jpg", requestBody) // API 명세에 따라 파트 이름은 "image"
            }
        } catch (e: Exception) {
            null
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
