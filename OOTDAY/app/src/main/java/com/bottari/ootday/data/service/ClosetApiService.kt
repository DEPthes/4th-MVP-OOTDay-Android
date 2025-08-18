package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.ClosetItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ClosetApiService {
    /**
     * 내 옷장 아이템 목록 조회 (카테고리별)
     */
    @GET("/api/cloth")
    suspend fun getMyCloset(
        @Query("category") category: String
    ): Response<List<ClosetItem>> // 옷 아이템 '목록'이므로 List로 받음

    /**
     * 옷 이미지 업로드
     */
    @Multipart
    @POST("/api/cloth")
    suspend fun uploadCloth(
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody // 👈 카테고리를 담을 파트 추가
    ): Response<ClosetItem> // 성공 시 업로드된 아이템 정보를 받음
}