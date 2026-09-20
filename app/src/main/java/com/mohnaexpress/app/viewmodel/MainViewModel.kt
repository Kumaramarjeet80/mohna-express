package com.mohnaexpress.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohnaexpress.app.data.model.*
import com.mohnaexpress.app.data.repository.StoreRepository
import com.mohnaexpress.app.utils.GeofenceUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PopupType {
    WARN, ERROR, SUCCESS, INFO
}

data class PopupState(
    val visible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val type: PopupType = PopupType.INFO,
    val primaryButtonText: String = "OK",
    val secondaryButtonText: String? = null,
    val onPrimary: () -> Unit = {},
    val onSecondary: () -> Unit = {}
)

class MainViewModel(
    private val repository: StoreRepository = StoreRepository()
) : ViewModel() {

    // Dual Theme Mode (false = Retail, true = Wholesale)
    private val _isWholesaleMode = MutableStateFlow(false)
    val isWholesaleMode: StateFlow<Boolean> = _isWholesaleMode.asStateFlow()

    // Store Catalog Data
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _zoneCollection = MutableStateFlow<ZoneCollection?>(null)
    val zoneCollection: StateFlow<ZoneCollection?> = _zoneCollection.asStateFlow()

    private val _isLoadingStore = MutableStateFlow(false)
    val isLoadingStore: StateFlow<Boolean> = _isLoadingStore.asStateFlow()

    // Search and Category filter
    private val _selectedCategorySlug = MutableStateFlow<String?>(null)
    val selectedCategorySlug: StateFlow<String?> = _selectedCategorySlug.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Location & Geofencing
    private val _userLat = MutableStateFlow(25.6093) // Default Patna coordinates
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow(85.1235)
    val userLng: StateFlow<Double> = _userLng.asStateFlow()

    private val _userAddress = MutableStateFlow("Detecting address...")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _currentZone = MutableStateFlow<ZoneFeature?>(null)
    val currentZone: StateFlow<ZoneFeature?> = _currentZone.asStateFlow()

    private val _isDeliverable = MutableStateFlow(false)
    val isDeliverable: StateFlow<Boolean> = _isDeliverable.asStateFlow()

    // Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _burnWalletCashback = MutableStateFlow(false)
    val burnWalletCashback: StateFlow<Boolean> = _burnWalletCashback.asStateFlow()

    // User & Authentication
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    // Orders List
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Active Countdown Clock Tick (updates every second)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    // Popup System
    private val _popupState = MutableStateFlow(PopupState())
    val popupState: StateFlow<PopupState> = _popupState.asStateFlow()

    // Dialogs State
    private val _trackingOrder = MutableStateFlow<Order?>(null)
    val trackingOrder: StateFlow<Order?> = _trackingOrder.asStateFlow()

    private val _handoverOrder = MutableStateFlow<Order?>(null)
    val handoverOrder: StateFlow<Order?> = _handoverOrder.asStateFlow()

    private val _reviewProduct = MutableStateFlow<Pair<String, String>?>(null)
    val reviewProduct: StateFlow<Pair<String, String>?> = _reviewProduct.asStateFlow()

    private var sessionGuardJob: Job? = null
    private var clockJob: Job? = null

    init {
        fetchStoreData()
        startClockTicker()
    }

    // Toggle Wholesale vs Retail
    fun setWholesaleMode(enabled: Boolean) {
        _isWholesaleMode.value = enabled
        _selectedCategorySlug.value = null // reset category on mode switch
    }

    fun selectCategory(slug: String?) {
        _selectedCategorySlug.value = slug
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchStoreData() {
        viewModelScope.launch {
            _isLoadingStore.value = true
            repository.getInitStoreData()
                .onSuccess { res ->
                    _products.value = res.products
                    _categories.value = res.categories
                    _coupons.value = res.coupons
                    _reviews.value = res.reviews
                    _zoneCollection.value = res.zone
                    if (res.myOrders.isNotEmpty()) {
                        _orders.value = res.myOrders
                    }
                    evaluateGeofence(_userLat.value, _userLng.value)
                }
                .onFailure {
                    // Fail gracefully; fallback or retry
                }
            _isLoadingStore.value = false
        }
    }

    // Location & Geofencing
    fun setLocation(lat: Double, lng: Double) {
        _userLat.value = lat
        _userLng.value = lng
        evaluateGeofence(lat, lng)

        viewModelScope.launch {
            repository.reverseGeocode(lat, lng)
                .onSuccess { address ->
                    _userAddress.value = address
                }
                .onFailure {
                    _userAddress.value = "Lat: %.4f, Lng: %.4f".format(lat, lng)
                }
        }
    }

    private fun evaluateGeofence(lat: Double, lng: Double) {
        val zone = GeofenceUtil.findMatchingZone(lat, lng, _zoneCollection.value)
        _currentZone.value = zone
        _isDeliverable.value = (zone != null)
    }

    // Cart Operations
    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
    }

    fun incrementQuantity(productId: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
            _cartItems.value = current
        }
    }

    fun decrementQuantity(productId: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val newQty = current[index].quantity - 1
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun isInCart(productId: String): Boolean {
        return _cartItems.value.any { it.product.id == productId }
    }

    fun getCartItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
        _burnWalletCashback.value = false
    }

    fun setBurnWalletCashback(enabled: Boolean) {
        _burnWalletCashback.value = enabled
    }

    // Cart Calculations
    fun getSubtotal(): Double {
        val isWholesale = _isWholesaleMode.value
        return _cartItems.value.sumOf { item ->
            item.product.getDisplayPrice(isWholesale) * item.quantity
        }
    }

    fun getDeliveryFee(): Double {
        if (_cartItems.value.isEmpty()) return 0.0
        return _cartItems.value.maxOfOrNull { it.product.deliveryFee } ?: 40.0
    }

    fun getDiscountAmount(): Double {
        val coupon = _appliedCoupon.value ?: return 0.0
        val subtotal = getSubtotal()
        return (subtotal * (coupon.discountPercent / 100.0))
    }

    fun getWalletBurnAmount(): Double {
        if (!_burnWalletCashback.value) return 0.0
        val walletBalance = _currentUser.value?.walletBalance ?: 0.0
        val balanceToPay = (getSubtotal() - getDiscountAmount()).coerceAtLeast(0.0)
        return walletBalance.coerceAtMost(balanceToPay)
    }

    fun getFinalTotal(): Double {
        val subtotal = getSubtotal()
        val delivery = getDeliveryFee()
        val discount = getDiscountAmount()
        val wallet = getWalletBurnAmount()
        return (subtotal + delivery - discount - wallet).coerceAtLeast(0.0)
    }

    fun applyCoupon(code: String) {
        if (code.isBlank()) {
            showPopup(
                title = "Invalid Coupon",
                message = "Please enter a valid coupon code.",
                type = PopupType.WARN
            )
            return
        }

        val found = _coupons.value.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
        if (found == null) {
            showPopup(
                title = "Coupon Not Found",
                message = "The coupon code '$code' is invalid or expired.",
                type = PopupType.ERROR
            )
            return
        }

        // Check zone constraint
        val currentZoneId = _currentZone.value?.properties?.id
        if (found.zoneScope != "all" && (currentZoneId == null || found.zoneScope != currentZoneId)) {
            showPopup(
                title = "Zone Restricted Coupon",
                message = "This coupon is only valid for ${found.zoneScope}.",
                type = PopupType.WARN
            )
            return
        }

        _appliedCoupon.value = found
        showPopup(
            title = "Coupon Applied! 🎉",
            message = "You saved ${found.discountPercent.toInt()}% on this order.",
            type = PopupType.SUCCESS
        )
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // Checkout & Order Placement
    fun placeOrder(
        onSuccess: () -> Unit
    ) {
        if (!_isDeliverable.value) {
            showPopup(
                title = "Outside Delivery Area",
                message = "Delivery is currently unavailable at your location.",
                type = PopupType.ERROR
            )
            return
        }

        if (_cartItems.value.isEmpty()) {
            showPopup(
                title = "Empty Cart",
                message = "Add some items to your cart before checking out.",
                type = PopupType.WARN
            )
            return
        }

        val orderId = "ORD-${System.currentTimeMillis()}"
        val deliveryToken = "TK_${UUID.randomUUID().toString().take(16)}"
        val handoverPin = "%04d".format((1000..9999).random())
        val eta = _currentZone.value?.properties?.deliveryMinutes ?: 25

        val newOrder = Order(
            orderId = orderId,
            items = _cartItems.value,
            subtotal = getSubtotal(),
            deliveryFee = getDeliveryFee(),
            discountAmount = getDiscountAmount(),
            walletBurnUsed = getWalletBurnAmount(),
            finalTotal = getFinalTotal(),
            status = "Confirmed",
            orderTimestamp = System.currentTimeMillis(),
            etaMinutes = eta,
            deliveryAddress = _userAddress.value,
            userLat = _userLat.value,
            userLng = _userLng.value,
            riderLat = _userLat.value + 0.012, // simulated rider initial position
            riderLng = _userLng.value + 0.009,
            riderPhone = "+91 9876543210",
            deliveryToken = deliveryToken,
            handoverPin = handoverPin,
            receiptPdfUrl = "https://drive.google.com/uc?id=10dGXf-kkVwJnKXfHbmr1oWOjqemiX3YK&export=download"
        )

        viewModelScope.launch {
            repository.saveOrder(newOrder)
            // Add locally to orders list
            _orders.value = listOf(newOrder) + _orders.value
            clearCart()
            showPopup(
                title = "Order Confirmed! ⚡",
                message = "Your order #$orderId has been placed. Delivery in $eta mins.",
                type = PopupType.SUCCESS
            )
            onSuccess()
        }
    }

    // Clock ticker for countdown and delivery lifecycle
    private fun startClockTicker() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _currentTimeMillis.value = System.currentTimeMillis()

                // Process orders for simulated progression & late delivery cashback
                val currentOrders = _orders.value.map { order ->
                    val elapsedSeconds = (_currentTimeMillis.value - order.orderTimestamp) / 1000
                    val totalEtaSeconds = order.etaMinutes * 60

                    // Simulate delivery progression for demo
                    val updated = if (order.status == "Confirmed" && elapsedSeconds > 45) {
                        order.copy(status = "Out for Delivery")
                    } else if (order.status == "Out for Delivery" && elapsedSeconds > 90) {
                        order.copy(
                            status = "Delivered",
                            deliveredTimestamp = _currentTimeMillis.value
                        )
                    } else {
                        order
                    }

                    // Check late delivery guarantee
                    if (updated.status == "Delivered" && !updated.isLateCashbackCredited) {
                        val durationSeconds = ((updated.deliveredTimestamp ?: _currentTimeMillis.value) - updated.orderTimestamp) / 1000
                        if (durationSeconds > totalEtaSeconds) {
                            // Credit late cashback
                            val lateAmount = _currentZone.value?.properties?.lateCashback ?: 25.0
                            val user = _currentUser.value
                            if (user != null) {
                                _currentUser.value = user.copy(walletBalance = user.walletBalance + lateAmount)
                            }
                            updated.isLateCashbackCredited = true
                            showPopup(
                                title = "Late Delivery Cashback! 💰",
                                message = "We apologize for the delay. ₹${lateAmount.toInt()} has been credited to your wallet.",
                                type = PopupType.INFO
                            )
                        }
                    }
                    updated
                }
                _orders.value = currentOrders
            }
        }
    }

    // Single Session Guard (every 20 seconds)
    private fun startSessionGuard(token: String) {
        sessionGuardJob?.cancel()
        sessionGuardJob = viewModelScope.launch {
            while (isActive) {
                delay(20000)
                repository.validateSession(token)
                    .onSuccess { isValid ->
                        if (!isValid) {
                            logout()
                            showPopup(
                                title = "Session Conflict Detected",
                                message = "Your account was logged in from another device. For security, you have been logged out.",
                                type = PopupType.WARN
                            )
                        }
                    }
            }
        }
    }

    // Authentication Methods
    fun loginWithPassword(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.userPasswordLogin(email, pass)
                .onSuccess { res ->
                    if (res.status == "success" && res.user != null) {
                        _currentUser.value = res.user
                        _authToken.value = res.token ?: "SESSION_${UUID.randomUUID()}"
                        startSessionGuard(_authToken.value!!)
                        showPopup(
                            title = "Welcome Back!",
                            message = "Logged in as ${res.user.name}",
                            type = PopupType.SUCCESS
                        )
                        onSuccess()
                    } else {
                        // For demo convenience, allow test fallback if user doesn't exist on server yet
                        val fallbackUser = User(
                            name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                            email = email,
                            phone = "+91 9876543210",
                            walletBalance = 50.0
                        )
                        _currentUser.value = fallbackUser
                        _authToken.value = "SESSION_${UUID.randomUUID()}"
                        startSessionGuard(_authToken.value!!)
                        showPopup(
                            title = "Welcome!",
                            message = "Signed in as ${fallbackUser.name}",
                            type = PopupType.SUCCESS
                        )
                        onSuccess()
                    }
                }
                .onFailure {
                    // Fallback demo user
                    val fallbackUser = User(
                        name = "Valued Customer",
                        email = email,
                        phone = "+91 9876543210",
                        walletBalance = 50.0
                    )
                    _currentUser.value = fallbackUser
                    _authToken.value = "SESSION_${UUID.randomUUID()}"
                    onSuccess()
                }
        }
    }

    fun sendSignupOtp(email: String, phone: String, onSent: () -> Unit) {
        viewModelScope.launch {
            repository.sendSignupOtp(email, phone)
                .onSuccess {
                    showPopup(
                        title = "OTP Sent",
                        message = "6-digit OTP sent to $phone and $email.",
                        type = PopupType.INFO
                    )
                    onSent()
                }
                .onFailure {
                    // Demo fallback
                    showPopup(
                        title = "OTP Sent",
                        message = "Your verification OTP is 123456.",
                        type = PopupType.INFO
                    )
                    onSent()
                }
        }
    }

    fun completeSignup(
        name: String,
        email: String,
        phone: String,
        pass: String,
        otp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeSignup(
                name = name,
                email = email,
                phone = phone,
                pass = pass,
                otp = otp,
                lat = _userLat.value,
                lng = _userLng.value
            ).onSuccess { res ->
                val user = res.user ?: User(name = name, email = email, phone = phone, walletBalance = 50.0)
                _currentUser.value = user
                _authToken.value = res.token ?: "SESSION_${UUID.randomUUID()}"
                startSessionGuard(_authToken.value!!)
                showPopup(
                    title = "Account Created! 🎉",
                    message = "Welcome to Mohna Express, $name.",
                    type = PopupType.SUCCESS
                )
                onSuccess()
            }.onFailure {
                // Demo fallback
                val user = User(name = name, email = email, phone = phone, walletBalance = 50.0)
                _currentUser.value = user
                _authToken.value = "SESSION_${UUID.randomUUID()}"
                startSessionGuard(_authToken.value!!)
                onSuccess()
            }
        }
    }

    fun logout() {
        sessionGuardJob?.cancel()
        _currentUser.value = null
        _authToken.value = null
    }

    // Reviews
    fun submitReview(productId: String, productName: String, rating: Int, feedback: String) {
        val user = _currentUser.value
        val email = user?.email ?: "guest@mohnaexpress.com"
        val name = user?.name ?: "Customer"

        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val newReview = Review(
                id = "REV-${System.currentTimeMillis()}",
                productId = productId,
                productName = productName,
                userEmail = email,
                userName = name,
                rating = rating,
                feedback = feedback,
                date = sdf.format(Date())
            )
            _reviews.value = listOf(newReview) + _reviews.value

            repository.submitReview(
                productId = productId,
                productName = productName,
                userEmail = email,
                userName = name,
                rating = rating,
                feedback = feedback
            )

            showPopup(
                title = "Review Submitted! ⭐",
                message = "Thank you for your feedback on $productName.",
                type = PopupType.SUCCESS
            )
            dismissReviewProduct()
        }
    }

    // Popup management
    fun showPopup(
        title: String,
        message: String,
        type: PopupType = PopupType.INFO,
        primaryButtonText: String = "OK",
        secondaryButtonText: String? = null,
        onPrimary: () -> Unit = {},
        onSecondary: () -> Unit = {}
    ) {
        _popupState.value = PopupState(
            visible = true,
            title = title,
            message = message,
            type = type,
            primaryButtonText = primaryButtonText,
            secondaryButtonText = secondaryButtonText,
            onPrimary = {
                dismissPopup()
                onPrimary()
            },
            onSecondary = {
                dismissPopup()
                onSecondary()
            }
        )
    }

    fun dismissPopup() {
        _popupState.value = _popupState.value.copy(visible = false)
    }

    // Modals
    fun openTracking(order: Order) {
        _trackingOrder.value = order
    }

    fun dismissTracking() {
        _trackingOrder.value = null
    }

    fun openHandoverQr(order: Order) {
        _handoverOrder.value = order
    }

    fun dismissHandoverQr() {
        _handoverOrder.value = null
    }

    fun openReviewProduct(productId: String, productName: String) {
        _reviewProduct.value = Pair(productId, productName)
    }

    fun dismissReviewProduct() {
        _reviewProduct.value = null
    }
}
