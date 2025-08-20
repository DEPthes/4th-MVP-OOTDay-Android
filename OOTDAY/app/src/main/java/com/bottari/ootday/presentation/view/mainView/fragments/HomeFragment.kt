package com.bottari.ootday.presentation.view.mainView.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.HomeViewModel
import com.bottari.ootday.data.model.mainModel.HomeViewModelFactory
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModel
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModelFactory
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.databinding.HomeFragmentBinding
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.DialogPictureFragment
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null
    val binding get() = _binding!!

    private var tempImageUri: Uri? = null

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(ClosetRepository(requireContext()))
    }


    private val pickImageFromGallery =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri -> onImageSelected(uri) }
            }
        }
    private val takePicture =
        registerForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                tempImageUri?.let { uri -> onImageSelected(uri) }
            }
        }
    private val requestGalleryPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted -> if (isGranted) openGallery() }
    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted -> if (isGranted) openCamera() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        // 최신 옷 아이템 목록을 관찰
        viewModel.recentClosetItems.observe(viewLifecycleOwner) { items ->
            // 화면에 있는 ImageView들을 리스트로 관리
            val imageViews = listOf(
                binding.itemImage1,
                binding.itemImage2,
                binding.itemImage3,
                binding.itemImage4
            )

            // 받아온 아이템 개수만큼 이미지를 순서대로 채워넣음
            items.forEachIndexed { index, item ->
                if (index < imageViews.size) {
                    loadImageWithGlide(imageViews[index], item.imageUrl)
                }
            }
        }

        // 옷장이 비었는지 여부를 관찰
        viewModel.isClosetEmpty.observe(viewLifecycleOwner) { isEmpty ->
            if (isEmpty) {
                // 옷장이 비었으면 -> '옷장 비었음' 이미지를 보여주고, 옷 그리드는 숨김
                binding.closetEmpty.visibility = View.VISIBLE
                binding.gridLayout.visibility = View.GONE
            } else {
                // 옷장에 아이템이 있으면 -> '옷장 비었음' 이미지는 숨기고, 옷 그리드를 보여줌
                binding.closetEmpty.visibility = View.GONE
                binding.gridLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setupClickListeners() {
        binding.plusClosetFrame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_firstClosetFragment)
        }

        binding.homeCardFrame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_firstClosetFragment)
        }

        binding.homeFindItemButton.setOnClickListener {
            showImageSelectionDialog()
        }
    }

    // ✨ 이미지 선택이 완료된 후 호출되는 핵심 함수
    private fun onImageSelected(uri: Uri) {
        val action = HomeFragmentDirections.actionHomeFragmentToSecondClosetPictureFragment(uri.toString())
        findNavController().navigate(action)
    }

    // ✨ URI를 MultipartBody.Part로 변환하는 헬퍼 함수
    private fun uriToMultipartBodyPart(uri: Uri): MultipartBody.Part? =
        try {
            val fileContent = requireContext().contentResolver.openInputStream(uri)?.readBytes()
            fileContent?.let {
                val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
            }
        } catch (e: Exception) {
            null
        }

    private fun showImageSelectionDialog() {
        DialogPictureFragment(
            onCameraButtonClick = { checkCameraPermission() },
            onGalleryButtonClick = { checkGalleryPermission() },
        ).show(childFragmentManager, "PictureSelectionDialog")
    }

    private fun checkGalleryPermission() {
        val permission =
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        if (ContextCompat.checkSelfPermission(requireContext(), permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestGalleryPermission.launch(permission)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        pickImageFromGallery.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
    }

    private fun openCamera() {
        tempImageUri = createImageUri()
        takePicture.launch(tempImageUri)
    }

    private fun createImageUri(): Uri? {
        val imageFile =
            File.createTempFile(
                "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())}_",
                ".jpg",
                requireContext().externalCacheDir,
            )
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imageFile)
    }

    private fun loadImageWithGlide(imageView: ImageView, url: String) {
        Glide.with(this)
            .load(url)
            .into(imageView)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRecentClosetItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
