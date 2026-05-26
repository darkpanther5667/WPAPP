package com.aistudio.sharmakhata.pqmzvk.data.remote

import com.aistudio.sharmakhata.pqmzvk.util.Constants
import com.aistudio.sharmakhata.pqmzvk.util.SessionManager
import com.aistudio.sharmakhata.pqmzvk.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val key = BuildConfig.MOBILE_API_KEY
            val builder = chain.request().newBuilder()
            if (key.isNotBlank()) builder.addHeader("X-API-KEY", key)
            val token = SessionManager.token
            if (!token.isNullOrBlank()) builder.addHeader("Authorization", "Bearer $token")
            val req = builder.build()
            chain.proceed(req)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
