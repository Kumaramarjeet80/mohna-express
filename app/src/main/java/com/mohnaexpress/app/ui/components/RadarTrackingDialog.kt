package com.mohnaexpress.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohnaexpress.app.data.model.Order
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.utils.GeofenceUtil

@Composable
fun RadarTrackingDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val distanceKm = remember(order.userLat, order.userLng, order.riderLat, order.riderLng) {
        val dist = GeofenceUtil.calculateDistanceKm(
            order.userLat, order.userLng,
            order.riderLat, order.riderLng
        )
        if (dist > 0) dist else 1.8
    }

    // Animated Radar Sweep angle
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Animated Rider pulse
    val riderPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rider_pulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimDark)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = CardSurfaceWhite
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Live Order Radar",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RetailPrimaryText
                                )
                            )
                            Text(
                                text = "Order #${order.orderId}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted
                                )
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Text(text = "✕", fontSize = 18.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Radar Screen Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Canvas Radar Sweep & Rings
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = (size.minDimension / 2) * 0.85f

                            // Draw Concentric Rings
                            listOf(0.33f, 0.66f, 1f).forEach { fraction ->
                                drawCircle(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                    radius = maxRadius * fraction,
                                    center = center,
                                    style = Stroke(width = 1.5f)
                                )
                            }

                            // Crosshair Grid lines
                            drawLine(
                                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                start = Offset(center.x, center.y - maxRadius),
                                end = Offset(center.x, center.y + maxRadius),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                start = Offset(center.x - maxRadius, center.y),
                                end = Offset(center.x + maxRadius, center.y),
                                strokeWidth = 1f
                            )

                            // Sweep Line
                            val rad = Math.toRadians(sweepAngle.toDouble())
                            val sweepEnd = Offset(
                                (center.x + maxRadius * Math.cos(rad)).toFloat(),
                                (center.y + maxRadius * Math.sin(rad)).toFloat()
                            )
                            drawLine(
                                color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                                start = center,
                                end = sweepEnd,
                                strokeWidth = 2.5f
                            )
                        }

                        // Customer Marker (Center 🏠)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏠", fontSize = 16.sp)
                            }
                            Text(
                                text = "You",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Rider Marker (Offset 🛵)
                        Box(
                            modifier = Modifier
                                .offset(x = 55.dp, y = (-40).dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((34 * riderPulse).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.85f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🛵", fontSize = 16.sp)
                                }
                                Text(
                                    text = "Rider (${String.format("%.1f", distanceKm)} km)",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Cards: Distance & ETA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Distance", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text(
                                    text = "${String.format("%.1f", distanceKm)} km",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RetailPrimaryText
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Status", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text(
                                    text = order.status,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RetailAccentEnd
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Call Rider Action Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${order.riderPhone}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusDeliverableText,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📞", fontSize = 16.sp)
                            Text(
                                text = "Call Delivery Partner (${order.riderPhone})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
