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

    suspend fun getMyCloset(): Result<List<ClosetItem>> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰 없음"))
            val response = closetApiService.getMyCloset("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("옷장 로딩 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createCloth(imagePart: MultipartBody.Part): Result<ClosetItem> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰 없음"))
            val response = closetApiService.createCloth("Bearer $token", imagePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("옷 생성 실패 (코드: ${response.code()}), 내용: $errorBody"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}


