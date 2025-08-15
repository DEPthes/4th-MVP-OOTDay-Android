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
import androidx.recyclerview.widget.GridLayoutManager
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.FirstClosetViewModel
import com.bottari.ootday.data.model.mainModel.FirstClosetViewModelFactory
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.databinding.FirstClosetFragmentBinding
import com.bottari.ootday.domain.model.DisplayableClosetItem
import com.bottari.ootday.presentation.view.mainView.adapters.ClosetAdapter
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.DialogPictureFragment
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirstClosetFragment : Fragment() {
    private lateinit var binding: FirstClosetFragmentBinding
    private var tempImageUri: Uri? = null

    private val viewModel: FirstClosetViewModel by viewModels {
        FirstClosetViewModelFactory(ClosetRepository())
    }

    private lateinit var closetAdapter: ClosetAdapter

    private val pickImagesFromGallery =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    if (intent.clipData != null) {
                        val count = intent.clipData!!.itemCount
                        for (i in 0 until count) {
                            val imageUri = intent.clipData!!.getItemAt(i).uri
                            uploadImageToFakeServer(imageUri)
                        }
                    } else if (intent.data != null) {
                        val imageUri = intent.data!!
                        uploadImageToFakeServer(imageUri)
                    }
                }
            }
        }

    private val takePicture =
        registerForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    Log.d("Camera", "카메라 촬영 성공: $uri")
                    uploadImageToFakeServer(uri)
                }
            } else {
                Log.d("Camera", "카메라 촬영 실패 또는 취소")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FirstClosetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupObservers()
        updateCategorySelection("상의")
        viewModel.loadItemsByCategory("상의")
    }

    private fun setupRecyclerView() {
        closetAdapter =
            ClosetAdapter(
                onItemClick = { item ->
                    when (item) {
                        is DisplayableClosetItem.AddButton -> {
                            showImageSelectionDialog()
                        }
                        is DisplayableClosetItem.ClosetData -> {
                            viewModel.toggleItemSelection(item)
                        }
                    }
                },
            )
        binding.closetRecyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = closetAdapter
        }
    }

    private val requestGalleryPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                openGalleryForImage()
            } else {
                Toast.makeText(context, "권한 거부. 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("CameraPermission", "카메라 권한 승인됨")
                openCameraForImage()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showPermissionRationaleDialog()
                } else {
                    showPermissionDeniedDialog()
                }
            }
        }

    private fun showPermissionDeniedDialog() {
        AlertDialog
            .Builder(requireContext())
            .setTitle("카메라 권한 필요")
            .setMessage("사진 촬영을 위해서는 카메라 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent =
                    Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireContext().packageName, null),
                    )
                startActivity(intent)
            }.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog
            .Builder(requireContext())
            .setTitle("카메라 권한 필요")
            .setMessage("이 기능을 사용하려면 카메라 권한이 필요합니다.")
            .setPositiveButton("확인") { _, _ ->
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showImageSelectionDialog() {
        val dialogFragment =
            DialogPictureFragment(
                onCameraButtonClick = {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        openCameraForImage()
                    } else {
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryButtonClick = {
                    val permission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                        openGalleryForImage()
                    } else {
                        requestGalleryPermission.launch(permission)
                    }
                },
            )
        dialogFragment.show(childFragmentManager, "imageSelectionDialog")
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImagesFromGallery.launch(intent)
    }

    private fun openCameraForImage() {
        tempImageUri = createImageUri()
        tempImageUri?.let { uri ->
            Log.d("CameraDebug", "생성된 Uri: $uri")
            takePicture.launch(uri)
        } ?: Log.e("CameraDebug", "Uri 생성 실패")
    }

    private fun createImageUri(): Uri? =
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "JPEG_${timeStamp}_"
            val tempFile =
                File.createTempFile(
                    fileName,
                    ".jpg",
                    requireContext().externalCacheDir,
                )
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempFile,
            )
        } catch (e: IllegalArgumentException) {
            Log.e("FileProvider", "FileProvider 설정 오류: ${e.message}")
            null
        }

    private fun uploadImageToFakeServer(imageUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val fileContent = requireContext().contentResolver.openInputStream(imageUri)?.readBytes()
            if (fileContent != null) {
                val requestBody = fileContent.toRequestBody("image/*".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
                viewModel.postClosetItem(multipart)
            }
        }
    }

    private fun setupListeners() {
        binding.closetTooltip.setOnClickListener {
            viewModel.onTooltipClicked()
        }

        binding.allSelectButton.setOnClickListener {
            viewModel.selectAllItemsInCurrentCategory()
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
            findNavController().navigate(R.id.action_firstClosetFragment_to_firstClosetMoodFragment)
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

        viewModel.onCategorySelected(selectedCategory, true)
    }

    private fun setupObservers() {
        viewModel.closetItems.observe(viewLifecycleOwner) { items ->
            (binding.closetRecyclerview.adapter as? ClosetAdapter)?.submitList(items)
        }

        viewModel.isTooltipVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.closetTooltip.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        viewModel.isStylingButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.stylingStartButton.isEnabled = isEnabled
        }

        viewModel.selectedItems.observe(viewLifecycleOwner) { selectedItems ->
            (binding.closetRecyclerview.adapter as? ClosetAdapter)?.setSelectedItems(selectedItems.map { it.uuid }.toSet())
            val isSelectionMode = selectedItems.isNotEmpty()
            (binding.closetRecyclerview.adapter as? ClosetAdapter)?.setSelectionMode(isSelectionMode)
        }

        viewModel.isAllSelectedInCurrentCategory.observe(viewLifecycleOwner) { isAllSelected ->
            val textColor =
                if (isAllSelected) {
                    ContextCompat.getColor(requireContext(), R.color.gray_100)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.gray_200)
                }
            binding.rememberMeCheckbox.isChecked = isAllSelected
            binding.selectAllText.setTextColor(textColor)
        }
    }
}
