package com.bottari.ootday.data.service

import com.bottari.ootday.domain.repository.dataClass.AuthCheckRequest
import com.bottari.ootday.domain.repository.dataClass.AuthCheckResponse
import com.bottari.ootday.domain.repository.dataClass.PhoneNumberAuthRequest
import com.bottari.ootday.domain.repository.dataClass.PhoneNumberAuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/request-phone-auth") // 실제 백엔드 API 엔드포인트로 변경하세요.
    suspend fun requestPhoneNumberAuth(
        @Body request: PhoneNumberAuthRequest,
    ): Response<PhoneNumberAuthResponse>

    @POST("api/auth/check-phone-auth") // 실제 백엔드 API 엔드포인트로 변경하세요.
    suspend fun checkPhoneNumberAuth(
        @Body request: AuthCheckRequest,
    ): Response<AuthCheckResponse>
}