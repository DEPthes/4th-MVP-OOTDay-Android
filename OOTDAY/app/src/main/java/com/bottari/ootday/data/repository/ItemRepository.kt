package com.bottari.ootday.data.repository

import android.content.Context
import com.bottari.ootday.data.service.ItemApiService
import com.bottari.ootday.domain.model.DataStoreManager
import com.bottari.ootday.domain.model.RecommendedItem
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody

class ItemRepository(context: Context) {
    private val dataStoreManager = DataStoreManager(context)
    private val retrofitClient = RetrofitClient(context)
    private val itemApiService: ItemApiService by lazy {
        retrofitClient.createService<ItemApiService>()
    }

    suspend fun findSimilarItem(imagePart: MultipartBody.Part): Result<RecommendedItem> {
        return try {
            val token = dataStoreManager.getToken.first()
            if (token.isNullOrBlank()) return Result.failure(Exception("토큰 없음"))

            val response = itemApiService.findSimilarItem("Bearer $token", imagePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("아이템 찾기 실패 (코드: ${response.code()}), 내용: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}