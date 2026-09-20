package com.mohnaexpress.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohnaexpress.app.data.model.Order
import com.mohnaexpress.app.ui.components.HandoverQrDialog
import com.mohnaexpress.app.ui.components.RadarTrackingDialog
import com.mohnaexpress.app.ui.components.ReviewDialog
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.viewmodel.MainViewModel

@Composable
fun OrdersScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current
    val orders by viewModel.orders.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val trackingOrder by viewModel.trackingOrder.collectAsState()
    val handoverOrder by viewModel.handoverOrder.collectAsState()
    val reviewProduct by viewModel.reviewProduct.collectAsState()

    // Modals
    if (trackingOrder != null) {
        RadarTrackingDialog(
            order = trackingOrder!!,
            onDismiss = { viewModel.dismissTracking() }
        )
    }

    if (handoverOrder != null) {
        HandoverQrDialog(
            order = handoverOrder!!,
            onDismiss = { viewModel.dismissHandoverQr() }
        )
    }

    if (reviewProduct != null) {
        ReviewDialog(
            productId = reviewProduct!!.first,
            productName = reviewProduct!!.second,
            onSubmit = { rating, feedback ->
                viewModel.submitReview(
                    productId = reviewProduct!!.first,
                    productName = reviewProduct!!.second,
                    rating = rating,
                    feedback = feedback
                )
            },
            onDismiss = { viewModel.dismissReviewProduct() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mohnaColors.backgroundGradient)
            .statusBarsPadding()
    ) {
        // App Bar
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
                    text = "📦 Order Tracking & History",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = mohnaColors.primaryText
                    )
                )
            }
        }

        if (orders.isEmpty()) {
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
                    Text(text = "📦", fontSize = 54.sp)
                    Text(
                        text = "No Orders Placed Yet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = mohnaColors.primaryText
                        )
                    )
                    Text(
                        text = "Orders you place will appear here with live countdown and tracking.",
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
                        Text(text = "⚡ Place First Order", fontWeight = FontWeight.Bold)
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
                items(orders, key = { it.orderId }) { order ->
                    OrderCard(
                        order = order,
                        currentTimeMillis = currentTimeMillis,
                        onTrackRider = { viewModel.openTracking(order) },
                        onViewHandoverQr = { viewModel.openHandoverQr(order) },
                        onWriteReview = { productId, productName ->
                            viewModel.openReviewProduct(productId, productName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    currentTimeMillis: Long,
    onTrackRider: () -> Unit,
    onViewHandoverQr: () -> Unit,
    onWriteReview: (productId: String, productName: String) -> Unit
) {
    val mohnaColors = LocalMohnaColors.current
    val isDelivered = order.status.equals("Delivered", ignoreCase = true)
    val totalEtaSeconds = order.etaMinutes * 60
    val elapsedSeconds = (currentTimeMillis - order.orderTimestamp) / 1000
    val remainingSeconds = (totalEtaSeconds - elapsedSeconds).coerceAtLeast(0)

    val remainingMinutes = remainingSeconds / 60
    val remainingSecs = remainingSeconds % 60

    // Delivered metrics
    val deliveredTimestamp = order.deliveredTimestamp ?: order.orderTimestamp + (order.etaMinutes * 60 * 1000)
    val deliveryDurationSeconds = ((deliveredTimestamp - order.orderTimestamp) / 1000).coerceAtLeast(0)
    val deliveredMin = deliveryDurationSeconds / 60
    val deliveredSec = deliveryDurationSeconds % 60
    val isOnTime = deliveryDurationSeconds <= totalEtaSeconds

    // 6-Hour Review Window Policy
    val sixHoursMillis = 6 * 60 * 60 * 1000L
    val isReviewEligible = isDelivered && ((currentTimeMillis - deliveredTimestamp) <= sixHoursMillis)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardSurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RetailPrimaryText
                        )
                    )
                    Text(
                        text = "${order.items.size} item(s) • Total: ₹${order.finalTotal.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDelivered) StatusDeliverableBg else Color(0xFFE0E7FF)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        color = if (isDelivered) StatusDeliverableText else RetailAccentStart,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown or On-Time / Late Delivery Tag
            if (!isDelivered) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "⏱️", fontSize = 16.sp)
                            Text(
                                text = "Estimated Delivery in",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E),
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "%02d:%02d".format(remainingMinutes, remainingSecs),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309)
                            )
                        )
                    }
                }
            } else {
                // Delivered Status Badge (Green on-time vs Red late)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOnTime) StatusDeliverableBg else StatusUndeliverableBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isOnTime) {
                                "✔ Delivered in ${deliveredMin}m ${deliveredSec}s (On Time)"
                            } else {
                                "⚠️ Delivered in ${deliveredMin}m ${deliveredSec}s (Late)"
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (isOnTime) StatusDeliverableText else StatusUndeliverableText,
                            fontSize = 12.sp
                        )

                        if (!isOnTime) {
                            Text(
                                text = "₹25 Late Cashback Credited",
                                color = StatusUndeliverableText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Radar Tracking & Handover QR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Radar Tracking
                OutlinedButton(
                    onClick = onTrackRider,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = mohnaColors.accentPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "📍", fontSize = 12.sp)
                        Text(text = "Track Rider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // View Handover QR Code
                Button(
                    onClick = onViewHandoverQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mohnaColors.accentPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔑", fontSize = 12.sp)
                        Text(text = "Handover QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6-Hour Verified Review Section
            if (isDelivered) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rate Purchased Products",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        if (!isReviewEligible) {
                            Text(
                                text = "Review window expired (6h limit)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    order.items.forEach { cartItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cartItem.product.name,
                                fontSize = 12.sp,
                                color = RetailPrimaryText,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { onWriteReview(cartItem.product.id, cartItem.product.name) },
                                enabled = isReviewEligible,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEF3C7),
                                    contentColor = Color(0xFFD97706),
                                    disabledContainerColor = Color(0xFFF1F5F9),
                                    disabledContentColor = Color(0xFF94A3B8)
                                )
                            ) {
                                Text(
                                    text = "⭐ Review",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
