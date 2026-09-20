package com.mohnaexpress.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Retail Theme Colors
val RetailBgStart = Color(0xFFE0E7FF)
val RetailBgMiddle = Color(0xFFFAE8FF)
val RetailBgEnd = Color(0xFFEDE9FE)
val RetailPrimaryText = Color(0xFF1E1B4B)
val RetailAccentStart = Color(0xFF4F46E5)
val RetailAccentEnd = Color(0xFF2563EB)
val RetailPrice = Color(0xFF4338CA)

// Wholesale Theme Colors
val WholesaleBgStart = Color(0xFFFFE4E6)
val WholesaleBgMiddle = Color(0xFFFFF1F2)
val WholesaleBgEnd = Color(0xFFFCE7F3)
val WholesalePrimaryText = Color(0xFF881337)
val WholesaleAccentStart = Color(0xFFE11D48)
val WholesaleAccentEnd = Color(0xFFBE123C)
val WholesalePrice = Color(0xFFBE123C)

// Shared & System Colors
val CardSurfaceWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF64748B)
val TextSecondary = Color(0xFF475569)
val BorderLight = Color(0xFFCBD5E1)
val ScrimDark = Color(0xA60F172A) // 65% opacity #0F172A

// Status & Badge Colors
val BadgeOutOfStock = Color(0xFFDC2626)
val BadgeDiscountBg = Color(0xFFFEE2E2)
val BadgeDiscountText = Color(0xFFDC2626)
val RatingBg = Color(0xFFFEF3C7)
val RatingText = Color(0xFFD97706)

val StatusDeliverableBg = Color(0xFFDCFCE7)
val StatusDeliverableText = Color(0xFF166534)
val StatusUndeliverableBg = Color(0xFFFEE2E2)
val StatusUndeliverableText = Color(0xFFDC2626)

// Modal Status Colors
val ModalWarnBg = Color(0xFFFEF3C7)
val ModalWarnIcon = Color(0xFFD97706)
val ModalErrorBg = Color(0xFFFEE2E2)
val ModalErrorIcon = Color(0xFFDC2626)
val ModalSuccessBg = Color(0xFFDCFCE7)
val ModalSuccessIcon = Color(0xFF166534)
val ModalInfoBg = Color(0xFFE0F2FE)
val ModalInfoIcon = Color(0xFF0284C7)

// Floating Pill & Dock Colors
val PillBackground = Color(0xE0FFFFFF) // 88% opacity
val NotificationRed = Color(0xFFEF4444)
val ChipActiveBg = Color(0xFF0F172A)
val ChipActiveText = Color(0xFFFFFFFF)
val SecondaryButtonBg = Color(0xFFF1F5F9)
val SecondaryButtonText = Color(0xFF334155)

// Gradient Helpers
val RetailBackgroundGradient = Brush.linearGradient(
    listOf(RetailBgStart, RetailBgMiddle, RetailBgEnd)
)
val WholesaleBackgroundGradient = Brush.linearGradient(
    listOf(WholesaleBgStart, WholesaleBgMiddle, WholesaleBgEnd)
)
val RetailButtonGradient = Brush.horizontalGradient(
    listOf(RetailAccentStart, RetailAccentEnd)
)
val WholesaleButtonGradient = Brush.horizontalGradient(
    listOf(WholesaleAccentStart, WholesaleAccentEnd)
)
