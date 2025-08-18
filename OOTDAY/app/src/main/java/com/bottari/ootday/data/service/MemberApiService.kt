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
import retrofit2.http.POST

interface MemberApiService {
    // 로그아웃 API (헤더 삭제)
    @POST("/api/members/logout")
    suspend fun logout(): Response<Unit>

    // 회원 탈퇴 API (헤더 삭제)
    @HTTP(method = "DELETE", path = "/api/members/withdraw", hasBody = true)
    suspend fun withdraw(@Body request: WithdrawRequest): Response<String>

    // 내 프로필 조회 API (헤더 삭제)
    @GET("/api/members/profile")
    suspend fun getMyProfile(): Response<ProfileResponse>

    // --- 아래는 인증이 필요 없는 API ---
    @POST("/api/members/join")
    suspend fun join(@Body request: SignUpData): Response<String>

    @POST("/api/members/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}