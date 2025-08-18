package com.bottari.ootday.data.repository

import android.content.Context
import android.util.Log
import com.bottari.ootday.data.service.ClosetApiService
import com.bottari.ootday.data.service.S3ApiService
import com.bottari.ootday.domain.model.ClosetItem
import com.bottari.ootday.domain.model.CreateClothRequest
import com.bottari.ootday.domain.model.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import kotlin.math.log

class ClosetRepository(context: Context) { // Context를 받도록 수정
    private val dataStoreManager = DataStoreManager(context)
    private val retrofitClient = RetrofitClient(context)
    private val closetApiService: ClosetApiService by lazy {
        retrofitClient.createService<ClosetApiService>()
    }
    private val s3ApiService: S3ApiService by lazy {
        retrofitClient.createService<S3ApiService>()
    }
    // ✨ 코디 조합 결과를 반환하는 가짜 API 호출 함수 추가
    suspend fun getCombinationResult(): List<String> {
        // 가짜 네트워크 딜레이
        delay(1500)

        // ✨ 실제로는 서버에서 이미지 URL 리스트를 받아옵니다.
        // 테스트를 위해 4개의 이미지 URL만 반환합니다.
        return listOf(
            "https://picsum.photos/id/10/400/600",
            "https://picsum.photos/id/20/400/600",
            "https://picsum.photos/id/30/400/600",
            "https://picsum.photos/id/40/400/600",
        )
    }

    suspend fun getMyCloset(category: String): Result<List<ClosetItem>> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰이 없습니다."))

            // 👇 API 호출 시 토큰 전달
            val response = closetApiService.getMyCloset("Bearer $token", category)
            if (response.isSuccessful && response.body() != null) {
                Log.d("ClosetDebug", "옷장 가져오기 성공")
                Result.success(response.body()!!)
            } else {
                Log.d("ClosetDebug", "옷장 가져오기 실패")
                Result.failure(Exception("옷장 아이템 로딩 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun uploadImageToS3(imagePart: MultipartBody.Part): Result<String> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰이 없습니다."))

            val uuid = UUID.randomUUID().toString()
            val domain = "CLOTHES"

            // 👇 API 호출 시 토큰 전달
            val response = s3ApiService.uploadFile("Bearer $token", imagePart, domain, uuid)
            if (response.isSuccessful && response.body() != null) {
                Log.d("ClosetDebug", "s3 업로드 성공")
                Result.success(response.body()!!)
            } else {
                Log.d("ClosetDebug", "s3 업로드 실패")
                Result.failure(Exception("S3 업로드 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun createClothItem(imageUrl: String, category: String): Result<ClosetItem> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰이 없습니다."))

            val request = CreateClothRequest(image = imageUrl, category = category)

            // 👇 API 호출 시 토큰 전달
            val response = closetApiService.createCloth("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Log.d("ClosetDebug", "서버 등록 성공")
                Result.success(response.body()!!)
            } else {
                Log.d("ClosetDebug", "서버 등록 실패 ${response.code()}")
                Result.failure(Exception("옷 정보 등록 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun uploadAndCreateCloth(imagePart: MultipartBody.Part, category: String): Result<ClosetItem> {
        val uploadResult = uploadImageToS3(imagePart)
        return uploadResult.fold(
            onSuccess = { imageUrl -> createClothItem(imageUrl, category) },
            onFailure = { exception -> Result.failure(exception) }
        )
    }
}


