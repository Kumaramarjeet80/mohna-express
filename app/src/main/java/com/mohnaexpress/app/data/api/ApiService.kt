package com.mohnaexpress.app.data.api

import com.google.gson.GsonBuilder
import com.mohnaexpress.app.data.model.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    // 1. Catalog & Initial Store Data Endpoints
    @GET("products")
    suspend fun getProducts(
        @Query("select") select: String = "*"
    ): List<Product>

    @GET("categories")
    suspend fun getCategories(
        @Query("select") select: String = "*"
    ): List<Category>

    @GET("coupons")
    suspend fun getCoupons(
        @Query("select") select: String = "*"
    ): List<Coupon>

    @GET("reviews")
    suspend fun getReviews(
        @Query("select") select: String = "*"
    ): List<Review>

    @GET("delivery_zones")
    suspend fun getDeliveryZones(
        @Query("select") select: String = "*"
    ): List<DeliveryZone>

    @GET("orders")
    suspend fun getUserOrders(
        @Query("user_email") email: String,
        @Query("order") order: String = "order_timestamp.desc",
        @Query("select") select: String = "*"
    ): List<Order>

    @GET("users")
    suspend fun getUserMetadata(
        @Query("email") email: String,
        @Query("select") select: String = "wallet_balance,session_token,status"
    ): List<User>

    // 2. Authentication Endpoints
    @GET("users")
    suspend fun loginUser(
        @Query("email") email: String,
        @Query("password") pass: String,
        @Query("select") select: String = "*"
    ): List<User>

    @PATCH("users")
    suspend fun updateUserSession(
        @Query("email") email: String,
        @Body body: UserSessionUpdate
    ): List<User>

    @GET("users")
    suspend fun getUserSessionToken(
        @Query("email") email: String,
        @Query("select") select: String = "session_token"
    ): List<UserSessionToken>

    @POST("users")
    suspend fun signupUser(
        @Body user: UserSignupRequest
    ): List<User>

    // 3. Atomic High-Concurrency Order Placement
    @POST("rpc/place_order")
    suspend fun placeOrderRpc(
        @Body payload: PlaceOrderRpcPayload
    ): PlaceOrderResponse

    @POST("orders")
    suspend fun insertOrder(
        @Body order: Order
    ): List<Order>

    // 4. Reviews & Coupons
    @GET("coupons")
    suspend fun getCouponByCode(
        @Query("code") code: String,
        @Query("select") select: String = "*"
    ): List<Coupon>

    @POST("reviews")
    suspend fun submitReview(
        @Body review: ReviewSubmissionRequest
    ): List<Review>

    @DELETE("reviews")
    suspend fun deleteReview(
        @Query("id") id: String
    ): retrofit2.Response<Unit>

    companion object {
        const val SUPABASE_URL = "https://YOUR_PROJECT_REF.supabase.co/rest/v1/"
        const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Supabase Mandatory Headers Interceptor
            val supabaseHeaderInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation")
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(supabaseHeaderInterceptor)
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}
