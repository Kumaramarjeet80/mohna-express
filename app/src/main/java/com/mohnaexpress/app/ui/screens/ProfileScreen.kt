package com.mohnaexpress.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.viewmodel.MainViewModel
import com.mohnaexpress.app.viewmodel.PopupType

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isWholesaleMode by viewModel.isWholesaleMode.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val walletBalance = currentUser?.walletBalance ?: 50.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mohnaColors.backgroundGradient)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
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
                    text = "👤 My Profile & Wallet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = mohnaColors.primaryText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User Info Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardSurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (currentUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(mohnaColors.buttonGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.name?.take(1) ?: "U").uppercase(),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = currentUser?.name ?: "Valued Customer",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RetailPrimaryText
                                )
                            )
                            Text(
                                text = currentUser?.email ?: "customer@mohnaexpress.com",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                            Text(
                                text = currentUser?.phone ?: "+91 9876543210",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "Orders: ${orders.size} placed",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RetailAccentEnd,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Guest User",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RetailPrimaryText
                                )
                            )
                            Text(
                                text = "Sign in to save addresses, earn cashback & sync orders",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }

                        Button(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mohnaColors.accentPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Sign In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cashback Wallet Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardSurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "👛", fontSize = 22.sp)
                        Text(
                            text = "Cashback Wallet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RetailPrimaryText
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusDeliverableBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = StatusDeliverableText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "₹${walletBalance.toInt()}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = mohnaColors.priceColor
                    )
                )

                Text(
                    text = "Use credits automatically during checkout to save instantly.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.showPopup(
                            title = "Wallet Top-up ⚡",
                            message = "Late delivery guarantees and promotional rewards are automatically deposited into your wallet.",
                            type = PopupType.INFO
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryButtonBg,
                        contentColor = SecondaryButtonText
                    )
                ) {
                    Text(text = "View Wallet Transactions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Saved Delivery Address
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardSurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🏠", fontSize = 18.sp)
                    Text(
                        text = "Current GPS Address",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RetailPrimaryText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userAddress,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App & Theme Settings
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardSurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "⚙️ App Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RetailPrimaryText
                    )
                )

                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))

                // Dual Theme Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Wholesale Bulk Store Mode",
                            fontWeight = FontWeight.SemiBold,
                            color = RetailPrimaryText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toggle rose-themed bulk catalog and wholesale pricing",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isWholesaleMode,
                        onCheckedChange = { viewModel.setWholesaleMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WholesaleAccentStart,
                            checkedTrackColor = WholesaleBgStart
                        )
                    )
                }

                HorizontalDivider(color = BorderLight.copy(alpha = 0.5f))

                // Single Session Security Guard Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Single Session Guard",
                            fontWeight = FontWeight.SemiBold,
                            color = RetailPrimaryText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Background monitor protects against multi-device conflicts",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusDeliverableBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE (20s)",
                            color = StatusDeliverableText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Logout Action
        if (currentUser != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.showPopup(
                        title = "Confirm Logout",
                        message = "Are you sure you want to log out of Mohna Express?",
                        type = PopupType.WARN,
                        primaryButtonText = "Log Out",
                        secondaryButtonText = "Cancel",
                        onPrimary = { viewModel.logout() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = NotificationRed
                )
            ) {
                Text(text = "🚪 Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
