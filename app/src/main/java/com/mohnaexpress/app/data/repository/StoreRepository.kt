package com.mohnaexpress.app.data.repository

import com.mohnaexpress.app.data.api.ApiService
import com.mohnaexpress.app.data.api.NominatimService
import com.mohnaexpress.app.data.model.*

class StoreRepository(
    private val apiService: ApiService = ApiService.create(),
    private val nominatimService: NominatimService = NominatimService.create()
) {

    suspend fun getInitStoreData(): Result<InitStoreResponse> = runCatching {
        apiService.getInitStoreData()
    }

    suspend fun validateSession(token: String): Result<Boolean> = runCatching {
        apiService.validateSession(token = token).valid
    }

    suspend fun userPasswordLogin(email: String, pass: String): Result<AuthResponse> = runCatching {
        apiService.userPasswordLogin(email = email, pass = pass)
    }

    suspend fun sendSignupOtp(email: String, phone: String): Result<AuthResponse> = runCatching {
        apiService.sendSignupOtp(email = email, phone = phone)
    }

    suspend fun completeSignup(
        name: String,
        email: String,
        phone: String,
        pass: String,
        otp: String,
        lat: Double,
        lng: Double
    ): Result<AuthResponse> = runCatching {
        apiService.completeSignup(
            name = name,
            email = email,
            phone = phone,
            pass = pass,
            otp = otp,
            lat = lat,
            lng = lng
        )
    }

    suspend fun saveOrder(order: Order): Result<SaveOrderResponse> = runCatching {
        apiService.saveOrder(order = order)
    }

    suspend fun validateCoupon(code: String, zone: String): Result<AuthResponse> = runCatching {
        apiService.validateCoupon(code = code, zone = zone)
    }

    suspend fun submitReview(
        productId: String,
        productName: String,
        userEmail: String,
        userName: String,
        rating: Int,
        feedback: String
    ): Result<AuthResponse> = runCatching {
        apiService.submitReview(
            productId = productId,
            productName = productName,
            userEmail = userEmail,
            userName = userName,
            rating = rating,
            feedback = feedback
        )
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): Result<String> = runCatching {
        val resp = nominatimService.reverseGeocode(lat = lat, lon = lng)
        resp.address?.toReadableString() ?: resp.displayName ?: "Lat: $lat, Lng: $lng"
    }
}
