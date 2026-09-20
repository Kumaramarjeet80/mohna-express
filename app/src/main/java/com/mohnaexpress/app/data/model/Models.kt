package com.mohnaexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("scope") val scope: String = "both",
    @SerializedName("retailPrice") val retailPrice: Double = 0.0,
    @SerializedName("wholesalePrice") val wholesalePrice: Double = 0.0,
    @SerializedName("mrp") val mrp: Double = 0.0,
    @SerializedName("stockQty") val stockQty: Int = 0,
    @SerializedName("deliveryFee") val deliveryFee: Double = 0.0,
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
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("discountPercent") val discountPercent: Double,
    @SerializedName("validUntil") val validUntil: String,
    @SerializedName("zoneScope") val zoneScope: String = "all"
)

data class Review(
    @SerializedName("id") val id: String = "",
    @SerializedName("productId") val productId: String = "",
    @SerializedName("productName") val productName: String = "",
    @SerializedName("userEmail") val userEmail: String = "",
    @SerializedName("userName") val userName: String = "",
    @SerializedName("rating") val rating: Int = 5,
    @SerializedName("feedback") val feedback: String = "",
    @SerializedName("date") val date: String = ""
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
    @SerializedName("name") val name: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("walletBalance") val walletBalance: Double = 0.0
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
    @SerializedName("orderId") val orderId: String,
    @SerializedName("items") val items: List<CartItem>,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("deliveryFee") val deliveryFee: Double,
    @SerializedName("discountAmount") val discountAmount: Double,
    @SerializedName("walletBurnUsed") val walletBurnUsed: Double,
    @SerializedName("finalTotal") val finalTotal: Double,
    @SerializedName("status") var status: String = "Placed",
    @SerializedName("orderTimestamp") val orderTimestamp: Long,
    @SerializedName("etaMinutes") val etaMinutes: Int = 30,
    @SerializedName("deliveredTimestamp") var deliveredTimestamp: Long? = null,
    @SerializedName("deliveryAddress") val deliveryAddress: String = "",
    @SerializedName("userLat") val userLat: Double = 0.0,
    @SerializedName("userLng") val userLng: Double = 0.0,
    @SerializedName("riderLat") var riderLat: Double = 0.0,
    @SerializedName("riderLng") var riderLng: Double = 0.0,
    @SerializedName("riderPhone") val riderPhone: String = "+91 9876543210",
    @SerializedName("deliveryToken") val deliveryToken: String = "",
    @SerializedName("handoverPin") val handoverPin: String = "1234",
    @SerializedName("receiptPdfUrl") val receiptPdfUrl: String? = null,
    @SerializedName("isLateCashbackCredited") var isLateCashbackCredited: Boolean = false
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
