package com.rx.vitreos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val VitreosDarkBg = Color(0xFF0A0E1A)
val VitreosGlass = Color(0xFF1A1F2E)
val VitreosAccent = Color(0xFF6366F1)
val VitreosAccentSecondary = Color(0xFF818CF8)
val VitreosText = Color(0xFFFFFFFF)
val VitreosTextSecondary = Color(0xFFA0AEC0)
val VitreosSuccess = Color(0xFF10B981)
val VitreosError = Color(0xFFEF4444)
val VitreosGradientStart = Color(0xFF1E1B4B)
val VitreosGradientEnd = Color(0xFF0A0E1A)

private val DarkColorScheme = darkColorScheme(
    primary = VitreosAccent,
    secondary = VitreosAccentSecondary,
    tertiary = VitreosSuccess,
    background = VitreosDarkBg,
    surface = VitreosGlass,
    onPrimary = VitreosText,
    onSecondary = VitreosText,
    onTertiary = VitreosText,
    onBackground = VitreosText,
    onSurface = VitreosText,
    error = VitreosError,
    onError = VitreosText
)

@Composable
fun VitreosTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VitreosDarkBg.toArgb()
            window.navigationBarColor = VitreosDarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}