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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bottari.ootday.R
import com.bottari.ootday.data.model.mainModel.FirstClosetViewModel
import com.bottari.ootday.data.model.mainModel.MoodPlaceViewModel
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
    private var _binding: FirstClosetFragmentBinding? = null
    private val binding get() = _binding!!
    private var tempImageUri: Uri? = null

    private val viewModel: FirstClosetViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = ClosetRepository(requireContext())
                return FirstClosetViewModel(repository) as T
            }
        }
    }

    private val sharedViewModel: MoodPlaceViewModel by activityViewModels()
    private lateinit var closetAdapter: ClosetAdapter

    // [핵심] 갤러리/카메라 앱에서 돌아왔을 때 처리하는 부분
    private val pickImagesFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = mutableListOf<Uri>()
                result.data?.let { intent ->
                    if (intent.clipData != null) { // 여러 장 선택 시
                        for (i in 0 until intent.clipData!!.itemCount) {
                            uris.add(intent.clipData!!.getItemAt(i).uri)
                        }
                    } else if (intent.data != null) { // 한 장 선택 시
                        uris.add(intent.data!!)
                    }
                }
                if (uris.isNotEmpty()) {
                    // ViewModel의 새 함수에 Uri '목록'을 한번에 전달
                    viewModel.uploadClothItems(requireContext(), uris)
                }
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    // 카메라는 한 장이므로, 한 개짜리 리스트를 만들어 전달
                    viewModel.uploadClothItems(requireContext(), listOf(uri))
                }
            }
        }

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openGalleryForImage()
            else Toast.makeText(context, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openCameraForImage()
            else Toast.makeText(context, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    // endregion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FirstClosetFragmentBinding.inflate(inflater, container, false)
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

        updateCategorySelection("상의") // 1. '상의' 버튼 UI를 선택 상태로 변경
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }

    private fun setupRecyclerView() {
        closetAdapter = ClosetAdapter { item ->
            when (item) {
                is DisplayableClosetItem.AddButton -> showImageSelectionDialog()
                is DisplayableClosetItem.ClosetData -> viewModel.toggleItemSelection(item)
            }
        }
        binding.closetRecyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = closetAdapter
        }
    }

    // 👇 [가장 중요한 수정 부분]
    // 각 버튼 클릭 시, UI 업데이트와 데이터 로딩을 '반드시' 함께 호출하도록 로직을 명확하게 수정했습니다.
    private fun setupListeners() {
        binding.closetTooltip.setOnClickListener { viewModel.onTooltipClicked() }
        binding.allSelectButton.setOnClickListener { viewModel.selectAllItemsInCurrentCategory() }

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

        binding.stylingStartButton.setOnClickListener {
            viewModel.selectedItems.value?.let { selectedItems ->
                if (selectedItems.isNotEmpty()) {
                    sharedViewModel.setSelectedClothes(selectedItems)
                    findNavController().navigate(R.id.action_firstClosetFragment_to_firstClosetMoodFragment)
                }
            }
        }
    }

    private fun setupObservers() {
        // ViewModel의 closetItems가 변경될 때마다 Adapter에 새 목록을 전달하여 UI를 갱신합니다.
        viewModel.closetItems.observe(viewLifecycleOwner) { items ->
            Log.d("ClosetDebug", "Fragment: 새로운 아이템 목록을 감지하고 Adapter에 전달합니다. 아이템 개수: ${items.size}")
            closetAdapter.submitList(items)
        }

        viewModel.isTooltipVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.closetTooltip.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        viewModel.isStylingButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.stylingStartButton.isEnabled = isEnabled
        }

        viewModel.selectedItems.observe(viewLifecycleOwner) { selectedItems ->
            closetAdapter.setSelectedItems(selectedItems.map { it.uuid }.toSet())
            closetAdapter.setSelectionMode(selectedItems.isNotEmpty())
        }

        viewModel.isAllSelectedInCurrentCategory.observe(viewLifecycleOwner) { isAllSelected ->
            val textColor =
                if (isAllSelected) ContextCompat.getColor(requireContext(), R.color.gray_100)
                else ContextCompat.getColor(requireContext(), R.color.gray_200)
            binding.rememberMeCheckbox.isChecked = isAllSelected
            binding.selectAllText.setTextColor(textColor)
        }
    }

    private fun updateCategorySelection(selectedCategory: String) {
        val gray100 = ContextCompat.getColor(requireContext(), R.color.gray_100)
        val gray200 = ContextCompat.getColor(requireContext(), R.color.gray_200)

        // 모든 카테고리 TextView를 Map으로 관리하여 코드를 간결하게 만듭니다.
        val categoryViews = mapOf(
            "상의" to binding.categoryTop,
            "하의" to binding.categoryBottom,
            "원피스" to binding.categoryDress,
            "신발" to binding.categoryShoes,
            "패션소품" to binding.categoryPassionItem,
            "악세서리" to binding.categoryDecorations
        )

        categoryViews.forEach { (category, textView) ->
            textView.setTextColor(if (category == selectedCategory) gray100 else gray200)
        }
    }

    // region [이미지 선택 다이얼로그 및 URI 생성 로직]
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

    private fun openCameraForImage() {
        tempImageUri = createImageUri()
        tempImageUri?.let { takePicture.launch(it) }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesFromGallery.launch(intent)
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
}