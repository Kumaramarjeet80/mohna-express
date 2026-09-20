package com.mohnaexpress.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mohnaexpress.app.data.model.CartItem
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.viewmodel.MainViewModel
import com.mohnaexpress.app.viewmodel.PopupType
import com.razorpay.Checkout
import org.json.JSONObject

@Composable
fun CheckoutScreen(
    viewModel: MainViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mohnaColors = LocalMohnaColors.current
    val cartItems by viewModel.cartItems.collectAsState()
    val isDeliverable by viewModel.isDeliverable.collectAsState()
    val currentZone by viewModel.currentZone.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val isWholesaleMode by viewModel.isWholesaleMode.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val burnWalletCashback by viewModel.burnWalletCashback.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var couponInput by remember { mutableStateOf("") }

    val subtotal = viewModel.getSubtotal()
    val deliveryFee = viewModel.getDeliveryFee()
    val discount = viewModel.getDiscountAmount()
    val walletBurn = viewModel.getWalletBurnAmount()
    val finalTotal = viewModel.getFinalTotal()

    fun launchRazorpayCheckout() {
        val activity = context as? Activity
        if (activity != null) {
            try {
                val co = Checkout()
                co.setKeyID("rzp_test_1DP5mmOlF5G5ag")
                val options = JSONObject().apply {
                    put("name", "Mohna Express")
                    put("description", "Quick-Commerce Order")
                    put("currency", "INR")
                    put("amount", (finalTotal * 100).toInt()) // paisa
                    val prefill = JSONObject().apply {
                        put("email", currentUser?.email ?: "customer@mohnaexpress.com")
                        put("contact", currentUser?.phone ?: "9876543210")
                    }
                    put("prefill", prefill)
                    val theme = JSONObject().apply {
                        put("color", if (isWholesaleMode) "#E11D48" else "#2563EB")
                    }
                    put("theme", theme)
                }
                co.open(activity, options)
            } catch (e: Exception) {
                // Fallback direct placement on mock/sandbox
                viewModel.placeOrder(onSuccess = onNavigateToOrders)
            }
        } else {
            viewModel.placeOrder(onSuccess = onNavigateToOrders)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mohnaColors.backgroundGradient)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = PillBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💳 Checkout & Payment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = mohnaColors.primaryText
                    )
                )

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text(
                            text = "Clear All",
                            color = NotificationRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            // Empty Cart State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🛒", fontSize = 54.sp)
                    Text(
                        text = "Your Cart is Empty",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = mohnaColors.primaryText
                        )
                    )
                    Text(
                        text = "Explore our store to add fresh items and bulk deals.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mohnaColors.accentPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "⚡ Start Shopping", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Delivery Address Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceWhite,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "📍", fontSize = 16.sp)
                                    Text(
                                        text = "Delivery Location",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = mohnaColors.primaryText
                                        )
                                    )
                                }

                                if (isDeliverable) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusDeliverableBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "COVERED",
                                            color = StatusDeliverableText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusUndeliverableBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "OUT OF ZONE",
                                            color = StatusUndeliverableText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = userAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            )

                            if (currentZone != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Zone: ${currentZone?.properties?.name} (Est. Delivery: ${currentZone?.properties?.deliveryMinutes} mins)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RetailAccentEnd,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }

                // Cart Items List
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        isWholesaleMode = isWholesaleMode,
                        onIncrement = { viewModel.incrementQuantity(item.product.id) },
                        onDecrement = { viewModel.decrementQuantity(item.product.id) }
                    )
                }

                // Coupon Code Section
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceWhite,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🏷️ Promo Coupon",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = mohnaColors.primaryText
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(StatusDeliverableBg)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Code '${appliedCoupon?.code}' Applied!",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusDeliverableText,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${appliedCoupon?.discountPercent?.toInt()}% discount applied to subtotal",
                                            color = StatusDeliverableText.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    TextButton(onClick = { viewModel.removeCoupon() }) {
                                        Text(
                                            text = "Remove",
                                            color = NotificationRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it.uppercase() },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(text = "Enter code (e.g. ALL, PATNA)", fontSize = 12.sp, color = TextMuted)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = mohnaColors.accentPrimary,
                                            unfocusedBorderColor = BorderLight
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.applyCoupon(couponInput)
                                            couponInput = ""
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mohnaColors.accentPrimary,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Text(text = "Apply", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Cashback Wallet Burn Section
                item {
                    val walletBalance = currentUser?.walletBalance ?: 0.0
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceWhite,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "👛", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "Use Cashback Wallet",
                                        fontWeight = FontWeight.Bold,
                                        color = mohnaColors.primaryText,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Available Balance: ₹${walletBalance.toInt()}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Checkbox(
                                checked = burnWalletCashback && walletBalance > 0,
                                onCheckedChange = { viewModel.setBurnWalletCashback(it) },
                                enabled = walletBalance > 0,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = mohnaColors.accentPrimary
                                )
                            )
                        }
                    }
                }

                // Bill Summary
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceWhite,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🧾 Bill Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = mohnaColors.primaryText
                                )
                            )

                            HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))

                            BillRow(label = "Items Subtotal", amount = "₹${subtotal.toInt()}")
                            BillRow(label = "Delivery Partner Fee", amount = "₹${deliveryFee.toInt()}")

                            if (discount > 0) {
                                BillRow(
                                    label = "Coupon Discount",
                                    amount = "-₹${discount.toInt()}",
                                    amountColor = StatusDeliverableText
                                )
                            }

                            if (walletBurn > 0) {
                                BillRow(
                                    label = "Cashback Credits Used",
                                    amount = "-₹${walletBurn.toInt()}",
                                    amountColor = StatusDeliverableText
                                )
                            }

                            HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "To Pay",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = mohnaColors.primaryText
                                    )
                                )
                                Text(
                                    text = "₹${finalTotal.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = mohnaColors.priceColor
                                    )
                                )
                            }
                        }
                    }
                }

                // Checkout & Payment CTA Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (!isDeliverable) {
                                viewModel.showPopup(
                                    title = "Outside Delivery Area",
                                    message = "We cannot deliver to your current GPS location yet.",
                                    type = PopupType.ERROR
                                )
                            } else {
                                launchRazorpayCheckout()
                            }
                        },
                        enabled = isDeliverable,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeliverable) mohnaColors.accentPrimary else Color(0xFF94A3B8),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⚡", fontSize = 18.sp)
                            Text(
                                text = if (isDeliverable) {
                                    "Place Order & Pay ₹${finalTotal.toInt()}"
                                } else {
                                    "Delivery Unavailable Outside Zone"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    isWholesaleMode: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val mohnaColors = LocalMohnaColors.current
    val unitPrice = item.product.getDisplayPrice(isWholesaleMode)
    val itemTotal = unitPrice * item.quantity

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.product.img.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.product.img,
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "⚡", fontSize = 20.sp)
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RetailPrimaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${unitPrice.toInt()} each",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
                Text(
                    text = "Total: ₹${itemTotal.toInt()}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = mohnaColors.priceColor
                    )
                )
            }

            // Quantity Modifier Controls (+ / -)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RetailPrimaryText)
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = RetailPrimaryText
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(mohnaColors.accentPrimary)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BillRow(
    label: String,
    amount: String,
    amountColor: Color = RetailPrimaryText
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        )
    }
}
