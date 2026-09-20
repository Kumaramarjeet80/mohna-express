package com.mohnaexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("scope") val scope: String = "both",
    @SerializedName("retail_price", alternate = ["retailPrice"]) val retailPrice: Double = 0.0,
    @SerializedName("wholesale_price", alternate = ["wholesalePrice"]) val wholesalePrice: Double = 0.0,
    @SerializedName("mrp") val mrp: Double = 0.0,
    @SerializedName("stock_qty", alternate = ["stockQty"]) val stockQty: Int = 0,
    @SerializedName("delivery_fee", alternate = ["deliveryFee"]) val deliveryFee: Double = 0.0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("specifications") val specifications: String? = null,
    @SerializedName("terms") val terms: String? = null,
    @SerializedName("img") val img: String? = null,
    @SerializedName("gallery") val gallery: List<String>? = emptyList()
) {
    fun getDisplayPrice(isWholesale: Boolean): Double {
        return if (isWholesale) wholesalePrice else retailPrice
    }

    val discountPercent: Int
        get() {
            val price = retailPrice
            return if (mrp > price && mrp > 0) {
                (((mrp - price) / mrp) * 100).toInt()
            } else 0
        }
}

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("scope") val scope: String = "both"
)

data class Coupon(
    @SerializedName("id") val id: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("discount_percent", alternate = ["discountPercent"]) val discountPercent: Double = 0.0,
    @SerializedName("valid_until", alternate = ["validUntil"]) val validUntil: String = "",
    @SerializedName("zone_scope", alternate = ["zoneScope"]) val zoneScope: String = "all"
)

data class Review(
    @SerializedName("id") val id: String = "",
    @SerializedName("product_id", alternate = ["productId"]) val productId: String = "",
    @SerializedName("product_name", alternate = ["productName"]) val productName: String = "",
    @SerializedName("user_email", alternate = ["userEmail"]) val userEmail: String = "",
    @SerializedName("user_name", alternate = ["userName"]) val userName: String = "",
    @SerializedName("rating") val rating: Int = 5,
    @SerializedName("feedback") val feedback: String = "",
    @SerializedName("created_at", alternate = ["date"]) val date: String = ""
)

data class ReviewSubmissionRequest(
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("feedback") val feedback: String
)

data class DeliveryZone(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("delivery_minutes", alternate = ["deliveryMinutes"]) val deliveryMinutes: Int = 30,
    @SerializedName("late_cashback", alternate = ["lateCashback"]) val lateCashback: Double = 25.0,
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>> = emptyList()
)

data class ZoneCollection(
    @SerializedName("type") val type: String = "FeatureCollection",
    @SerializedName("features") val features: List<ZoneFeature> = emptyList()
)

data class ZoneFeature(
    @SerializedName("type") val type: String = "Feature",
    @SerializedName("properties") val properties: ZoneProperties = ZoneProperties(),
    @SerializedName("geometry") val geometry: ZoneGeometry = ZoneGeometry()
)

data class ZoneProperties(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("deliveryMinutes") val deliveryMinutes: Int = 30,
    @SerializedName("lateCashback") val lateCashback: Double = 25.0
)

data class ZoneGeometry(
    @SerializedName("type") val type: String = "Polygon",
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>> = emptyList()
)

data class InitStoreResponse(
    @SerializedName("products") val products: List<Product> = emptyList(),
    @SerializedName("categories") val categories: List<Category> = emptyList(),
    @SerializedName("coupons") val coupons: List<Coupon> = emptyList(),
    @SerializedName("reviews") val reviews: List<Review> = emptyList(),
    @SerializedName("myReviews") val myReviews: List<Review> = emptyList(),
    @SerializedName("zone") val zone: ZoneCollection? = null,
    @SerializedName("myOrders") val myOrders: List<Order> = emptyList(),
    @SerializedName("walletBalance") val walletBalance: Double = 0.0
)

data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("password") val password: String? = null,
    @SerializedName("address") val address: String = "",
    @SerializedName("reg_lat", alternate = ["lat"]) val lat: Double = 0.0,
    @SerializedName("reg_lng", alternate = ["lng"]) val lng: Double = 0.0,
    @SerializedName("wallet_balance", alternate = ["walletBalance"]) val walletBalance: Double = 50.0,
    @SerializedName("session_token", alternate = ["sessionToken"]) val sessionToken: String? = null,
    @SerializedName("status") val status: String? = "active"
)

data class UserSessionUpdate(
    @SerializedName("session_token") val sessionToken: String,
    @SerializedName("login_lat") val loginLat: Double? = null,
    @SerializedName("login_lng") val loginLng: Double? = null
)

data class UserSessionToken(
    @SerializedName("session_token") val sessionToken: String? = null
)

data class UserSignupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("reg_lat") val regLat: Double,
    @SerializedName("reg_lng") val regLng: Double,
    @SerializedName("session_token") val sessionToken: String,
    @SerializedName("wallet_balance") val walletBalance: Double = 50.0
)

data class AuthResponse(
    @SerializedName("status") val status: String = "error",
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("valid") val valid: Boolean? = null
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class Order(
    @SerializedName("order_id", alternate = ["orderId"]) val orderId: String,
    @SerializedName("items") val items: List<CartItem>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("delivery_fee", alternate = ["deliveryFee"]) val deliveryFee: Double,
    @SerializedName("discount_amount", alternate = ["discountAmount"]) val discountAmount: Double,
    @SerializedName("wallet_burn_used", alternate = ["walletBurnUsed"]) val walletBurnUsed: Double,
    @SerializedName("final_total", alternate = ["finalTotal"]) val finalTotal: Double,
    @SerializedName("status") var status: String = "Placed",
    @SerializedName("order_timestamp", alternate = ["orderTimestamp", "created_at"]) val orderTimestamp: Long,
    @SerializedName("eta_minutes", alternate = ["etaMinutes"]) val etaMinutes: Int = 30,
    @SerializedName("delivered_timestamp", alternate = ["deliveredTimestamp"]) var deliveredTimestamp: Long? = null,
    @SerializedName("delivery_address", alternate = ["deliveryAddress"]) val deliveryAddress: String = "",
    @SerializedName("user_lat", alternate = ["userLat"]) val userLat: Double = 0.0,
    @SerializedName("user_lng", alternate = ["userLng"]) val userLng: Double = 0.0,
    @SerializedName("rider_lat", alternate = ["riderLat"]) var riderLat: Double = 0.0,
    @SerializedName("rider_lng", alternate = ["riderLng"]) var riderLng: Double = 0.0,
    @SerializedName("rider_phone", alternate = ["riderPhone"]) val riderPhone: String = "+91 9876543210",
    @SerializedName("delivery_token", alternate = ["deliveryToken"]) val deliveryToken: String = "",
    @SerializedName("handover_pin", alternate = ["handoverPin"]) val handoverPin: String = "1234",
    @SerializedName("receipt_pdf_url", alternate = ["receiptPdfUrl"]) val receiptPdfUrl: String? = null,
    @SerializedName("is_late_cashback_credited", alternate = ["isLateCashbackCredited"]) var isLateCashbackCredited: Boolean = false,
    @SerializedName("user_email") val userEmail: String? = null
)

data class PlaceOrderRpcPayload(
    @SerializedName("p_order_id") val pOrderId: String,
    @SerializedName("p_items") val pItems: List<CartItem>,
    @SerializedName("p_wallet_deduction") val pWalletDeduction: Double,
    @SerializedName("p_subtotal") val pSubtotal: Double,
    @SerializedName("p_delivery_fee") val pDeliveryFee: Double,
    @SerializedName("p_discount_amount") val pDiscountAmount: Double,
    @SerializedName("p_final_total") val pFinalTotal: Double,
    @SerializedName("p_user_email") val pUserEmail: String,
    @SerializedName("p_user_address") val pUserAddress: String,
    @SerializedName("p_user_lat") val pUserLat: Double,
    @SerializedName("p_user_lng") val pUserLng: Double,
    @SerializedName("p_delivery_token") val pDeliveryToken: String,
    @SerializedName("p_handover_pin") val pHandoverPin: String,
    @SerializedName("p_eta_minutes") val pEtaMinutes: Int
)

data class PlaceOrderResponse(
    @SerializedName("status") val status: String = "success",
    @SerializedName("order_id", alternate = ["orderId"]) val orderId: String? = null,
    @SerializedName("message") val message: String? = null
)

data class SaveOrderResponse(
    @SerializedName("status") val status: String = "error",
    @SerializedName("expiryTimestamp") val expiryTimestamp: Long? = null,
    @SerializedName("etaMinutes") val etaMinutes: Int? = null,
    @SerializedName("receiptPdfUrl") val receiptPdfUrl: String? = null,
    @SerializedName("deliveryToken") val deliveryToken: String? = null,
    @SerializedName("message") val message: String? = null
)

data class SessionStatusResponse(
    @SerializedName("valid") val valid: Boolean = false
)

// Nominatim OpenStreetMap Reverse Geocoding Model
data class NominatimReverseResponse(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("address") val address: NominatimAddress? = null
)

data class NominatimAddress(
    @SerializedName("road") val road: String? = null,
    @SerializedName("suburb") val suburb: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("postcode") val postcode: String? = null
) {
    fun toReadableString(): String {
        val parts = listOfNotNull(road, suburb, city, state, postcode)
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "Delivery Address"
    }
}
