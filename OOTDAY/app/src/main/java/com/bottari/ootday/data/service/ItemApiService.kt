package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.RecommendedItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ItemApiService {
    @Multipart
    @POST("/api/item")
    suspend fun findSimilarItem(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<RecommendedItem>
}