package com.bottari.ootday.data.model.mainModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.MultipartBody

// ✨ 나중에 서버로 보낼 이미지 파일 데이터를 보관하는 ViewModel
class SecondClosetViewModel : ViewModel() {

    // ✨ 처리된 이미지 데이터를 LiveData로 저장
    private val _imagePart = MutableLiveData<MultipartBody.Part?>()
    val imagePart: LiveData<MultipartBody.Part?> = _imagePart

    // ✨ Fragment에서 이미지 데이터를 ViewModel로 전달하는 함수
    fun setImageData(imagePart: MultipartBody.Part) {
        _imagePart.value = imagePart
    }
}