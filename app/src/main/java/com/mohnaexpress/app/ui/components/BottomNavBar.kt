package com.mohnaexpress.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohnaexpress.app.ui.theme.LocalMohnaColors
import com.mohnaexpress.app.ui.theme.NotificationRed
import com.mohnaexpress.app.ui.theme.PillBackground

enum class NavDestination(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    CHECKOUT("Checkout", "💳"),
    ORDERS("My Orders", "📦"),
    PROFILE("Profile", "👤")
}

@Composable
fun BottomNavBar(
    currentDestination: NavDestination,
    cartItemCount: Int,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(62.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(32.dp),
        color = PillBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination
                val activeBgColor by animateColorAsState(
                    targetValue = if (isSelected) mohnaColors.accentPrimary.copy(alpha = 0.12f) else Color.Transparent,
                    label = "nav_item_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) mohnaColors.accentPrimary else Color(0xFF64748B),
                    label = "nav_item_text"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(activeBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDestinationSelected(destination) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = destination.icon,
                                fontSize = 18.sp
                            )
                            // Red Badge for Checkout total items
                            if (destination == NavDestination.CHECKOUT && cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 10.dp, y = (-8).dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(NotificationRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$cartItemCount",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (isSelected) {
                            Text(
                                text = destination.label,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
