package com.mohnaexpress.app.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mohnaexpress.app.data.model.Product
import com.mohnaexpress.app.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    isWholesaleMode: Boolean,
    isInCart: Boolean,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product) -> Unit,
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current
    val isOutOfStock = product.stockQty <= 0
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isOutOfStock) 0.97f else 1f,
        label = "card_scale"
    )

    val displayPrice = product.getDisplayPrice(isWholesaleMode)
    val mrp = product.mrp
    val discountPercent = product.discountPercent

    // Base64 image decoding caching
    val base64Bitmap = remember(product.img) {
        if (product.img?.startsWith("data:image") == true) {
            try {
                val base64Str = product.img.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = !isOutOfStock,
                interactionSource = interactionSource,
                indication = null
            ) { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardSurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Product Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                if (base64Bitmap != null) {
                    Image(
                        bitmap = base64Bitmap,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!product.img.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.img,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "⚡",
                        fontSize = 36.sp
                    )
                }

                // Top-Left Rating Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RatingBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⭐ 4.8",
                        color = RatingText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top-Right Scope Tag (if wholesale mode shows Wholesale badge)
                if (product.scope == "wholesale" || isWholesaleMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(WholesaleBgStart)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📦 Bulk",
                            color = WholesalePrice,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Absolute Out of Stock Banner
                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BadgeOutOfStock)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OUT OF STOCK",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RetailPrimaryText
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category tag
            Text(
                text = product.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price & MRP strike-through row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "₹${displayPrice.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = mohnaColors.priceColor
                    )
                )

                if (mrp > displayPrice) {
                    Text(
                        text = "₹${mrp.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )

                    if (discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BadgeDiscountBg)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$discountPercent% OFF",
                                color = BadgeDiscountText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Buttons: "Add" & "Buy Now"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add / Added Toggle Button
                val addBgColor by animateColorAsState(
                    targetValue = if (isInCart) StatusDeliverableBg else Color.White,
                    label = "add_btn_bg"
                )
                val addTextColor by animateColorAsState(
                    targetValue = if (isInCart) StatusDeliverableText else mohnaColors.accentPrimary,
                    label = "add_btn_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(addBgColor)
                        .border(
                            width = 1.dp,
                            color = if (isInCart) StatusDeliverableText else mohnaColors.accentPrimary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isOutOfStock) { onAddToCart(product) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isInCart) "✔ Added" else "🛒 Add",
                        color = addTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Buy Now Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isOutOfStock) SolidColor(Color(0xFFCBD5E1)) else mohnaColors.buttonGradient
                        )
                        .clickable(enabled = !isOutOfStock) { onBuyNow(product) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ Buy Now",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
