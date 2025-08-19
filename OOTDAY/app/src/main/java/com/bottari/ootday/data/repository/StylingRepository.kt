package com.bottari.ootday.data.repository

import android.content.Context
import com.bottari.ootday.data.service.MoodDto
import com.bottari.ootday.data.service.PlaceDto
import com.bottari.ootday.data.service.StylingApiService
import com.bottari.ootday.domain.model.ClothingItemDto
import com.bottari.ootday.domain.model.DataStoreManager
import com.bottari.ootday.domain.model.StylingRequest
import kotlinx.coroutines.flow.first

class StylingRepository(context: Context) {
    private val dataStoreManager = DataStoreManager(context)
    private val retrofitClient = RetrofitClient(context)
    private val stylingApiService: StylingApiService by lazy {
        retrofitClient.createService<StylingApiService>()
    }


    suspend fun getMoodKeywords(): Result<List<MoodDto>> {
        return try {
            // 인터셉터가 토큰을 처리하므로 바로 API 호출
            val response = stylingApiService.getMoods()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("무드 키워드 로딩 실패"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addMoodKeyword(moodName: String): Result<Unit> {
        return try {
            val response = stylingApiService.addMood(moodName)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("무드 키워드 추가 실패"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getPlaceKeywords(): Result<List<PlaceDto>> {
        return try {
            val response = stylingApiService.getPlaces()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else { Result.failure(Exception("장소 키워드 로딩 실패")) }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addPlaceKeyword(placeName: String): Result<Unit> {
        return try {
            val response = stylingApiService.addPlace(placeName)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else { Result.failure(Exception("장소 키워드 추가 실패")) }
        } catch (e: Exception) { Result.failure(e) }
    }

    // 최종 코디 조합 요청 함수
    suspend fun getStylingResult(request: StylingRequest): Result<List<ClothingItemDto>> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰 없음"))

            val response = stylingApiService.getStylingResult("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("코디 결과 요청 실패"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

}