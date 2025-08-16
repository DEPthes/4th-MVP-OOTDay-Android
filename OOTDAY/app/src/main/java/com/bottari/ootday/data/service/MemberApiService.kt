package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.WithdrawRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST


interface MemberApiService {
    // 로그아웃 API
    @POST("/api/members/logout") // 실제 로그아웃 엔드포인트 경로로 수정하세요.
    suspend fun logout(
        @Header("Authorization") token: String // 헤더에 토큰을 추가
    ): Response<Unit> // 로그아웃은 특별한 응답 바디가 없는 경우가 많으므로 Unit으로 설정

    // 회원 탈퇴 API (추가)
    @HTTP(method = "DELETE", path = "api/members/withdraw", hasBody = true)
    suspend fun withdraw(
        @Header("Authorization") token: String,
        @Body request: WithdrawRequest
    ): Response<String>
}