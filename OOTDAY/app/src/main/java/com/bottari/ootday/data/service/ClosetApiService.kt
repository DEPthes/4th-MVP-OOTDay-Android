package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.ClosetItem
import com.bottari.ootday.domain.model.CreateClothRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ClosetApiService {
    /**
     * 내 옷장 아이템 목록 조회 (카테고리별)
     */
    @GET("/api/cloth")
    suspend fun getMyCloset(
        @Header("Authorization") token: String
    ): Response<List<ClosetItem>> // 옷 아이템 '목록'이므로 List로 받음

    /**
     * 옷 이미지 업로드
     */
    @Multipart // 👈 Multipart 요청
    @POST("/api/cloth")
    suspend fun createCloth(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part // 👈 'file' 이라는 이름의 이미지 파일 파트 하나만 전송
    ): Response<ClosetItem>

    /**
     * [핵심 추가] uuid를 사용하여 옷 아이템을 삭제하는 API
     */
    @DELETE("/api/cloth/{uuid}")
    suspend fun deleteCloth(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: String
    ): Response<Unit>
}