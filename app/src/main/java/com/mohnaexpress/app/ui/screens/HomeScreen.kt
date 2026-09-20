package com.mohnaexpress.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohnaexpress.app.data.model.Category
import com.mohnaexpress.app.data.model.Product
import com.mohnaexpress.app.ui.components.AppHeader
import com.mohnaexpress.app.ui.components.ProductCard
import com.mohnaexpress.app.ui.theme.*
import com.mohnaexpress.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mohnaColors = LocalMohnaColors.current
    val isWholesaleMode by viewModel.isWholesaleMode.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategorySlug by viewModel.selectedCategorySlug.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDeliverable by viewModel.isDeliverable.collectAsState()
    val currentZone by viewModel.currentZone.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoadingStore by viewModel.isLoadingStore.collectAsState()

    // Filter products based on mode, category, and search query
    val filteredProducts = remember(products, isWholesaleMode, selectedCategorySlug, searchQuery) {
        products.filter { p ->
            val scopeMatch = if (isWholesaleMode) {
                p.scope.equals("wholesale", ignoreCase = true) || p.scope.equals("both", ignoreCase = true)
            } else {
                p.scope.equals("retail", ignoreCase = true) || p.scope.equals("both", ignoreCase = true)
            }

            val categoryMatch = selectedCategorySlug == null || p.category.equals(selectedCategorySlug, ignoreCase = true)

            val queryMatch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true) ||
                    (p.description?.contains(searchQuery, ignoreCase = true) == true)

            scopeMatch && categoryMatch && queryMatch
        }
    }

    // Filter categories matching current mode
    val activeCategories = remember(categories, isWholesaleMode) {
        categories.filter { c ->
            if (isWholesaleMode) {
                c.scope.equals("wholesale", ignoreCase = true) || c.scope.equals("both", ignoreCase = true)
            } else {
                c.scope.equals("retail", ignoreCase = true) || c.scope.equals("both", ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mohnaColors.backgroundGradient)
            .statusBarsPadding()
    ) {
        // App Header
        AppHeader(
            isLoggedIn = currentUser != null,
            userName = currentUser?.name,
            notificationCount = 3,
            onAuthClick = onNavigateToAuth,
            onNotificationClick = {
                viewModel.showPopup(
                    title = "Flash Deals ⚡",
                    message = "Free 15-minute delivery on all orders above ₹199 today!",
                    type = com.mohnaexpress.app.viewmodel.PopupType.INFO
                )
            }
        )

        // Delivery Zone Status Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isDeliverable) StatusDeliverableBg else StatusUndeliverableBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = if (isDeliverable) "📍" else "⚠️", fontSize = 14.sp)
                    Column {
                        Text(
                            text = if (isDeliverable) {
                                "Deliverable to ${currentZone?.properties?.name ?: "Coverage Area"}"
                            } else {
                                "Undeliverable outside coverage"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDeliverable) StatusDeliverableText else StatusUndeliverableText
                        )
                        Text(
                            text = userAddress,
                            fontSize = 10.sp,
                            color = if (isDeliverable) StatusDeliverableText.copy(alpha = 0.8f) else StatusUndeliverableText.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }

                if (isDeliverable) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusDeliverableText)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${currentZone?.properties?.deliveryMinutes ?: 30} MINS",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Mode Segmented Toggle: Retail Store vs Wholesale Bulk
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(48.dp)
                .shadow(2.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = CardSurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Retail Store Pill
                val retailBg by animateColorAsState(
                    targetValue = if (!isWholesaleMode) RetailAccentStart else Color.Transparent,
                    label = "retail_pill"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(retailBg)
                        .clickable { viewModel.setWholesaleMode(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛍️ Retail Store",
                        color = if (!isWholesaleMode) Color.White else RetailPrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Wholesale Bulk Pill
                val wholesaleBg by animateColorAsState(
                    targetValue = if (isWholesaleMode) WholesaleAccentStart else Color.Transparent,
                    label = "wholesale_pill"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(wholesaleBg)
                        .clickable { viewModel.setWholesaleMode(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📦 Wholesale Bulk",
                        color = if (isWholesaleMode) Color.White else WholesalePrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Bar with instant debounce
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = CardSurfaceWhite
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isWholesaleMode) "Search bulk supplies, cases, cartons..." else "Search groceries, drinks, fresh items...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = mohnaColors.accentPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }

        // Category Chips Row (Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" Chip
            val isAllSelected = selectedCategorySlug == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isAllSelected) ChipActiveBg else Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isAllSelected) ChipActiveBg else BorderLight,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.selectCategory(null) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🌟 All Items",
                    color = if (isAllSelected) ChipActiveText else Color(0xFF334155),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Category list items
            activeCategories.forEach { category ->
                val isSelected = selectedCategorySlug == category.slug
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ChipActiveBg else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) ChipActiveBg else BorderLight,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectCategory(category.slug) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category.name.replaceFirstChar { it.uppercase() },
                        color = if (isSelected) ChipActiveText else Color(0xFF334155),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Product Catalog Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoadingStore && products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = mohnaColors.accentPrimary)
                }
            } else if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = mohnaColors.primaryText
                            )
                        )
                        Text(
                            text = "Try searching for a different item or category",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted
                            )
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isWholesaleMode = isWholesaleMode,
                            isInCart = viewModel.isInCart(product.id),
                            onAddToCart = { viewModel.addToCart(it) },
                            onBuyNow = {
                                viewModel.addToCart(it)
                                onNavigateToCheckout()
                            },
                            onCardClick = {
                                viewModel.showPopup(
                                    title = product.name,
                                    message = "${product.description ?: "High quality item"}\n\nDelivery Fee: ₹${product.deliveryFee.toInt()} | Stock: ${product.stockQty} left",
                                    type = com.mohnaexpress.app.viewmodel.PopupType.INFO,
                                    primaryButtonText = "Add to Cart",
                                    secondaryButtonText = "Close",
                                    onPrimary = { viewModel.addToCart(product) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
