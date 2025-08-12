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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ✨ 다운로드 상태를 UI에 알리기 위한 LiveData (SingleLiveEvent 패턴 권장)
    private val _downloadStatus = MutableLiveData<String>()
    val downloadStatus: LiveData<String> = _downloadStatus

    // ✨ 이미지 다운로드 로직
    fun downloadImages(context: Context) {
        // 현재 이미지 URL이 없으면 함수 종료
        if (_imageUrls.value.isNullOrEmpty()) {
            _downloadStatus.value = "다운로드할 이미지가 없습니다."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // 파일 I/O 작업이므로 IO 스레드에서 실행
            try {
                _imageUrls.value?.forEach { url ->
                    // Glide를 사용해 이미지를 비트맵으로 다운로드
                    val bitmap =
                        Glide
                            .with(context)
                            .asBitmap()
                            .load(url)
                            .submit()
                            .get()

                    // MediaStore를 사용해 갤러리에 저장
                    saveImageToGallery(context, bitmap, "OOTDay_${System.currentTimeMillis()}.jpg")
                }
                _downloadStatus.postValue("모든 이미지를 저장했습니다.")
            } catch (e: Exception) {
                _downloadStatus.postValue("저장에 실패했습니다: ${e.message}")
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
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
    }

    fun loadCombination() {
        // 이미 데이터가 있다면 중복으로 로드하지 않음
        if (!_imageUrls.value.isNullOrEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Repository에서 가짜 이미지 URL 리스트를 가져옵니다.
                val result = repository.getCombinationResult()
                _imageUrls.value = result
            } catch (e: Exception) {
                // TODO: 오류 처리
            } finally {
                _isLoading.value = false
            }
        }
    }
}
