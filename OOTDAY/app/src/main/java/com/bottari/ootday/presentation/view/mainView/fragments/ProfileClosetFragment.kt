package com.bottari.ootday.presentation.view.mainView.fragments

import android.Manifest
import android.app.Activity
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
import androidx.recyclerview.widget.GridLayoutManager
import com.bottari.ootday.R
import com.bottari.ootday.data.model.profileModel.ProfileClosetViewModel
import com.bottari.ootday.data.model.profileModel.ProfileClosetViewModelFactory
import com.bottari.ootday.data.repository.ClosetRepository
import com.bottari.ootday.databinding.ProfileClosetFragmentBinding
import com.bottari.ootday.domain.model.DisplayableClosetItem
import com.bottari.ootday.presentation.view.mainView.adapters.ClosetAdapter
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.DeleteConfirmationDialogFragment
import com.bottari.ootday.presentation.view.mainView.fragments.dialog.DialogPictureFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileClosetFragment : Fragment() {
    private var _binding: ProfileClosetFragmentBinding? = null
    private val binding get() = _binding!!
    private var tempImageUri: Uri? = null

    private val viewModel: ProfileClosetViewModel by viewModels {
        ProfileClosetViewModelFactory(ClosetRepository(requireContext()))
    }
    private lateinit var closetAdapter: ClosetAdapter

    // --- ActivityResultLaunchers (갤러리, 카메라, 권한) ---
    private val pickImagesFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = mutableListOf<Uri>()
                result.data?.let { intent ->
                    if (intent.clipData != null) {
                        for (i in 0 until intent.clipData!!.itemCount) { uris.add(intent.clipData!!.getItemAt(i).uri) }
                    } else if (intent.data != null) {
                        uris.add(intent.data!!)
                    }
                }
                if (uris.isNotEmpty()) viewModel.uploadClothItems(requireContext(), uris)
            }
        }
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) tempImageUri?.let { viewModel.uploadClothItems(requireContext(), listOf(it)) }
        }
    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) openGalleryForImage() }
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) openCameraForImage() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ProfileClosetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        updateCategorySelection("상의")
    }

    private fun setupRecyclerView() {
        closetAdapter = ClosetAdapter { item ->
            when (item) {
                is DisplayableClosetItem.AddButton -> showImageSelectionDialog()
                is DisplayableClosetItem.ClosetData -> viewModel.toggleItemSelection(item)
            }
        }
        // 👇 XML 레이아웃에 정의된 RecyclerView ID를 사용해주세요.
        binding.closetRecyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = closetAdapter
        }
    }

    private fun setupListeners() {
        binding.closetDeleteButton.setOnClickListener {
            if (viewModel.selectedItems.value.orEmpty().isNotEmpty()) {
                val dialog = DeleteConfirmationDialogFragment {
                    viewModel.deleteSelectedItems()
                }
                dialog.show(childFragmentManager, "DeleteConfirmationDialog")
            }
        }

        binding.allSelectButton.setOnClickListener {
            viewModel.selectAllItemsInCurrentCategory()
        }

        val categoryClickListener = View.OnClickListener { view ->
            val category = when (view.id) {
                R.id.category_top -> "상의"
                R.id.category_bottom -> "하의"
                R.id.category_dress -> "원피스"
                R.id.category_shoes -> "신발"
                R.id.category_passion_item -> "패션소품"
                R.id.category_decorations -> "악세서리"
                else -> return@OnClickListener
            }
            updateCategorySelection(category)
            viewModel.showItemsForCategory(category)
        }
        binding.categoryTop.setOnClickListener(categoryClickListener)
        binding.categoryBottom.setOnClickListener(categoryClickListener)
        binding.categoryDress.setOnClickListener(categoryClickListener)
        binding.categoryShoes.setOnClickListener(categoryClickListener)
        binding.categoryPassionItem.setOnClickListener(categoryClickListener)
        binding.categoryDecorations.setOnClickListener(categoryClickListener)
    }

    private fun observeViewModel() {
        viewModel.closetItems.observe(viewLifecycleOwner) { items ->
            closetAdapter.submitList(items)
        }
        viewModel.selectedItems.observe(viewLifecycleOwner) { selectedItems ->
            closetAdapter.setSelectedItems(selectedItems.map { it.uuid }.toSet())
        }
        viewModel.isSelectionMode.observe(viewLifecycleOwner) { isSelectionMode ->
            closetAdapter.setSelectionMode(isSelectionMode)
        }
        viewModel.isDeleteButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.closetDeleteButton.isEnabled = isEnabled
        }
        viewModel.isAllSelectedInCurrentCategory.observe(viewLifecycleOwner) { isAllSelected ->
            binding.rememberMeCheckbox.isChecked = isAllSelected
            val textColor = if (isAllSelected) ContextCompat.getColor(requireContext(), R.color.gray_100)
            else ContextCompat.getColor(requireContext(), R.color.gray_200)
            binding.selectAllText.setTextColor(textColor)
        }
    }

    private fun updateCategorySelection(selectedCategory: String) {
        val gray100 = ContextCompat.getColor(requireContext(), R.color.gray_100)
        val gray200 = ContextCompat.getColor(requireContext(), R.color.gray_200)
        val categoryViews = mapOf(
            "상의" to binding.categoryTop, "하의" to binding.categoryBottom, "원피스" to binding.categoryDress,
            "신발" to binding.categoryShoes, "패션소품" to binding.categoryPassionItem, "악세서리" to binding.categoryDecorations
        )
        categoryViews.forEach { (category, textView) ->
            textView.setTextColor(if (category == selectedCategory) gray100 else gray200)
        }
    }

    // --- 👇 [핵심] 비어있던 이미지 선택 관련 함수들의 전체 로직을 복원합니다. ---
    private fun showImageSelectionDialog() {
        val dialog = DialogPictureFragment(
            onCameraButtonClick = {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCameraForImage()
                } else {
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            },
            onGalleryButtonClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
                else Manifest.permission.READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                    openGalleryForImage()
                } else {
                    requestGalleryPermission.launch(permission)
                }
            }
        )
        dialog.show(childFragmentManager, "DialogPictureFragment")
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesFromGallery.launch(intent)
    }

    private fun openCameraForImage() {
        tempImageUri = createImageUri()
        tempImageUri?.let { takePicture.launch(it) }
    }

    private fun createImageUri(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", requireContext().externalCacheDir)
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", tempFile)
        } catch (e: Exception) {
            Log.e("FileProvider", "URI 생성 실패", e)
            null
        }
    }
    // --- 👆 여기까지 로직 복원 ---

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}