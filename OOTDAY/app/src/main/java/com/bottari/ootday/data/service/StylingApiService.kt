package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.ClothingItemDto
import com.bottari.ootday.domain.model.StylingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface StylingApiService {
    // 무드 키워드 전체 조회
    @GET("/api/styling/keyword/mood")
    suspend fun getMoods(): Response<List<MoodDto>>

    // 무드 키워드 추가
    @POST("/api/styling/keyword/mood")
    suspend fun addMood(@Query("input") moodName: String): Response<Unit>

    // 장소 키워드 전체 조회
    @GET("/api/styling/keyword/place")
    suspend fun getPlaces(): Response<List<PlaceDto>>

    // 장소 키워드 추가
    @POST("/api/styling/keyword/place")
    suspend fun addPlace(@Query("input") placeName: String): Response<Unit>

    // 최종 코디 조합 요청 API
    @POST("/api/styling")
    suspend fun getStylingResult(
        @Header("Authorization") token: String,
        @Body request: StylingRequest
    ):  Response<List<List<ClothingItemDto>>> // 응답은 추천된 옷 아이템 목록
}