package com.mohnaexpress.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class MohnaThemeColors(
    val isWholesale: Boolean,
    val backgroundGradient: Brush,
    val primaryText: Color,
    val buttonGradient: Brush,
    val priceColor: Color,
    val accentPrimary: Color,
    val accentSecondary: Color
)

val LocalMohnaColors = staticCompositionLocalOf {
    MohnaThemeColors(
        isWholesale = false,
        backgroundGradient = RetailBackgroundGradient,
        primaryText = RetailPrimaryText,
        buttonGradient = RetailButtonGradient,
        priceColor = RetailPrice,
        accentPrimary = RetailAccentStart,
        accentSecondary = RetailAccentEnd
    )
}

private val LightColorScheme = lightColorScheme(
    primary = RetailAccentStart,
    secondary = RetailAccentEnd,
    background = Color.Transparent,
    surface = CardSurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = RetailPrimaryText,
    onSurface = RetailPrimaryText
)

@Composable
fun MohnaExpressTheme(
    isWholesaleMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val mohnaColors = if (isWholesaleMode) {
        MohnaThemeColors(
            isWholesale = true,
            backgroundGradient = WholesaleBackgroundGradient,
            primaryText = WholesalePrimaryText,
            buttonGradient = WholesaleButtonGradient,
            priceColor = WholesalePrice,
            accentPrimary = WholesaleAccentStart,
            accentSecondary = WholesaleAccentEnd
        )
    } else {
        MohnaThemeColors(
            isWholesale = false,
            backgroundGradient = RetailBackgroundGradient,
            primaryText = RetailPrimaryText,
            buttonGradient = RetailButtonGradient,
            priceColor = RetailPrice,
            accentPrimary = RetailAccentStart,
            accentSecondary = RetailAccentEnd
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    CompositionLocalProvider(LocalMohnaColors provides mohnaColors) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
