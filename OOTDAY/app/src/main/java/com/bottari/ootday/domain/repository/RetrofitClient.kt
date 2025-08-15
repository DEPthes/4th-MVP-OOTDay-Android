package com.bottari.ootday.domain.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 찾은 Base URL을 입력하세요.
    private const val BASE_URL = "http://13.125.211.246:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> Kotlin 객체 변환
            .build()
    }

    fun <T> createService(service: Class<T>): T = retrofit.create(service)
}
