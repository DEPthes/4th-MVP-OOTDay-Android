package com.bottari.ootday.presentation.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottari.ootday.data.repository.ClosetRepository
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream

class ClosetResultViewModel(
    private val repository: ClosetRepository,
) : ViewModel() {
    private val _imageUrls = MutableLiveData<List<String>>()
    val imageUrls: LiveData<List<String>> = _imageUrls

    // 다운로드 버튼 활성화 상태를 관리하는 LiveData
    private val _isDownloadEnabled = MutableLiveData<Boolean>(true)
    val isDownloadEnabled: LiveData<Boolean> get() = _isDownloadEnabled

    // ✨ 다운로드 상태를 UI에 알리기 위한 LiveData (SingleLiveEvent 패턴 권장)
    private val _downloadStatus = MutableLiveData<String>()
    val downloadStatus: LiveData<String> = _downloadStatus

    fun setImageUrls(urls: List<String>) {
        _imageUrls.value = urls
    }

    fun downloadImages(context: Context) {
        if (_isDownloadEnabled.value == false) return // 중복 다운로드 방지
        if (_imageUrls.value.isNullOrEmpty()) {
            _downloadStatus.value = "다운로드할 이미지가 없습니다."
            return
        }

        // 다운로드 시작과 동시에 버튼 비활성화
        _isDownloadEnabled.value = false

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _imageUrls.value?.forEach { url ->
                    val bitmap = Glide.with(context).asBitmap().load(url).submit().get()
                    saveImageToGallery(context, bitmap, "OOTDay_Styling_${System.currentTimeMillis()}.jpg")
                }
                _downloadStatus.postValue("모든 이미지를 저장했습니다.")
            } catch (e: Exception) {
                _downloadStatus.postValue("저장에 실패했습니다: ${e.message}")
                _isDownloadEnabled.postValue(true) // 실패 시 다시 시도할 수 있도록 버튼 활성화
            }
        }
    }

    // ✨ 비트맵을 갤러리에 저장하는 함수
    private fun saveImageToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
    ) {
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val contentValues =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

        val resolver = context.contentResolver
        val uri = resolver.insert(collection, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
    }
}
