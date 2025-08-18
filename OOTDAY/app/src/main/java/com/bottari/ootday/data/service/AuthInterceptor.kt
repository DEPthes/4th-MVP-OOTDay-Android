package com.bottari.ootday.data.service

import com.bottari.ootday.domain.model.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 기존 요청
        val originalRequest = chain.request()

        // DataStore에서 저장된 토큰을 가져옴 (runBlocking은 여기서처럼 즉시 필요할 때 사용)
        val token = runBlocking {
            dataStoreManager.getToken.first()
        }

        // 토큰이 있는 경우, 헤더에 "Authorization"을 추가한 새 요청을 생성
        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        // 새로 만든 요청으로 통신을 계속 진행
        return chain.proceed(newRequest)
    }
}