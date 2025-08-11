package com.bottari.ootday.presentation.view.mainView.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModel
import com.bottari.ootday.data.model.mainModel.SecondClosetViewModelFactory
import com.bottari.ootday.databinding.HomeFragmentBinding
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.dialogPictureFragment
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
    private val binding get() = _binding!!

    // ✨ SecondCloset 기능의 상태를 관리할 ViewModel
    private val viewModel: SecondClosetViewModel by viewModels { SecondClosetViewModelFactory() }

    private var tempImageUri: Uri? = null

    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> onImageSelected(uri) }
        }
    }
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri -> onImageSelected(uri) }
        }
    }
    private val requestGalleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) openGallery() }
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) openCamera() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()

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
        viewLifecycleOwner.lifecycleScope.launch {
            val imagePart = uriToMultipartBodyPart(uri)
            if (imagePart != null) {
                viewModel.setImageData(imagePart)
                // ✨ 올바른 Directions 클래스 사용
                val action = HomeFragmentDirections.actionHomeFragmentToSecondClosetPictureFragment(uri.toString())
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "이미지 처리 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✨ URI를 MultipartBody.Part로 변환하는 헬퍼 함수
    private fun uriToMultipartBodyPart(uri: Uri): MultipartBody.Part? {
        return try {
            val fileContent = requireContext().contentResolver.openInputStream(uri)?.readBytes()
            fileContent?.let {
                val requestBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun showImageSelectionDialog() {
        dialogPictureFragment(
            onCameraButtonClick = { checkCameraPermission() },
            onGalleryButtonClick = { checkGalleryPermission() }
        ).show(childFragmentManager, "PictureSelectionDialog")
    }

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) openGallery() else requestGalleryPermission.launch(permission)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera() else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun openGallery() {
        pickImageFromGallery.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
    }

    private fun openCamera() {
        tempImageUri = createImageUri()
        takePicture.launch(tempImageUri)
    }

    private fun createImageUri(): Uri? {
        val imageFile = File.createTempFile("JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())}_", ".jpg", requireContext().externalCacheDir)
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imageFile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}