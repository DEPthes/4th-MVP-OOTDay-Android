package com.bottari.ootday.data.service

import com.bottari.ootday.data.model.loginModel.LoginRequest
import com.bottari.ootday.data.model.loginModel.LoginResponse
import com.bottari.ootday.data.model.loginModel.ProfileResponse
import com.bottari.ootday.data.model.signupModel.SignUpData
import com.bottari.ootday.domain.model.WithdrawRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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

    @POST("/api/members/join")
    suspend fun join(
        @Body request: SignUpData
    ): Response<String> // 성공 시 UUID (String) 응답

    // 로그인 API
    @POST("/api/members/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // 내 프로필 조회 API
    @GET("/api/members/profile")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<ProfileResponse>

}