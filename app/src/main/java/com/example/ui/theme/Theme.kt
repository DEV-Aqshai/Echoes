package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MintBlue,
    onPrimary = PremiumBlack,
    primaryContainer = MintBlueGlow,
    onPrimaryContainer = OffWhite,
    secondary = BeigeYellow,
    onSecondary = PremiumBlack,
    secondaryContainer = DarkCard,
    onSecondaryContainer = BeigeYellow,
    background = PremiumBlack,
    onBackground = OffWhite,
    surface = PremiumBlack,
    onSurface = OffWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = OffWhite,
    outline = DarkCardBorder,
    error = DangerRed,
    onError = OffWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PremiumBlack.toArgb()
                window.navigationBarColor = PremiumBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
