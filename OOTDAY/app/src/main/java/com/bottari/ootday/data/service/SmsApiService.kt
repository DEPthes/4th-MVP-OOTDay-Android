package com.bottari.ootday.data.service

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface SmsApiService {
    @POST("/sms/send")
    suspend fun sendSmsVerification(
        @Query("phoneNumber") phoneNumber: String
    ): Response<String>

    /**
     * SMS 인증번호 확인 API
     */
    @POST("/sms/verify")
    suspend fun verifySmsCode(
        @Query("phoneNumber") phoneNumber: String,
        @Query("code") code: String
    ): Response<String>
}