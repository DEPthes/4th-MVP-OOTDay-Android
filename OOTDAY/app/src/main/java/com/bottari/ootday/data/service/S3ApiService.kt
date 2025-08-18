package com.bottari.ootday.data.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface S3ApiService {
    @Multipart
    @POST("/api/s3/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Query("domain") domain: String,
        @Query("uuid") uuid: String
    ): Response<String> // 성공 시 이미지 URL(String)을 받음
}