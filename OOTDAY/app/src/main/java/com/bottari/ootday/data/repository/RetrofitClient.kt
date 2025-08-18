package com.bottari.ootday.data.repository

import android.content.Context
import com.bottari.ootday.BuildConfig
import com.bottari.ootday.data.service.AuthInterceptor
import com.bottari.ootday.domain.model.DataStoreManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitClient(context: Context) {
    private val dataStoreManager = DataStoreManager(context)

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply { /* ... */ }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(dataStoreManager)) // 👈 인증 인터셉터 등록
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://13.125.211.246:8080/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}