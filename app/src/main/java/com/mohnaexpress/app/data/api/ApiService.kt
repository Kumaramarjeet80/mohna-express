package com.mohnaexpress.app.data.api

import com.google.gson.GsonBuilder
import com.mohnaexpress.app.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("exec")
    suspend fun getInitStoreData(
        @Query("action") action: String = "getInitStoreData"
    ): InitStoreResponse

    @GET("exec")
    suspend fun validateSession(
        @Query("action") action: String = "validateSession",
        @Query("token") token: String
    ): SessionStatusResponse

    @GET("exec")
    suspend fun userPasswordLogin(
        @Query("action") action: String = "userPasswordLogin",
        @Query("email") email: String,
        @Query("password") pass: String
    ): AuthResponse

    @GET("exec")
    suspend fun sendSignupOtp(
        @Query("action") action: String = "sendSignupOtp",
        @Query("email") email: String,
        @Query("phone") phone: String
    ): AuthResponse

    @GET("exec")
    suspend fun completeSignup(
        @Query("action") action: String = "completeSignup",
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("phone") phone: String,
        @Query("password") pass: String,
        @Query("otp") otp: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): AuthResponse

    @POST("exec")
    suspend fun saveOrder(
        @Query("action") action: String = "saveOrder",
        @Body order: Order
    ): SaveOrderResponse

    @GET("exec")
    suspend fun validateCoupon(
        @Query("action") action: String = "validateCoupon",
        @Query("code") code: String,
        @Query("zone") zone: String
    ): AuthResponse

    @GET("exec")
    suspend fun submitReview(
        @Query("action") action: String = "submitReview",
        @Query("productId") productId: String,
        @Query("productName") productName: String,
        @Query("userEmail") userEmail: String,
        @Query("userName") userName: String,
        @Query("rating") rating: Int,
        @Query("feedback") feedback: String
    ): AuthResponse

    companion object {
        private const val BASE_URL = "https://script.google.com/macros/s/AKfycbwISME2C5UqmBGIH5uqRZPjh357sXmlM2fppxm3_rEss8qVoCRsZh5d6QdfTED13jpt/"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}
