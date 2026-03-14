package com.vitascan.ai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MedicalBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = MedicalBlueSurface,
    onPrimaryContainer = MedicalBlueDark,
    secondary = HealthGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = HealthGreenSurface,
    onSecondaryContainer = HealthGreenDark,
    tertiary = RiskMedium,
    background = SurfaceWhite,
    onBackground = NeutralGrayDark,
    surface = SurfaceCard,
    onSurface = NeutralGrayDark,
    surfaceVariant = NeutralGrayLight,
    onSurfaceVariant = NeutralGray,
    error = RiskHigh,
    errorContainer = Color(0xFFFFEBEE),
    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = MedicalBlueLight,
    onPrimary = MedicalBlueDark,
    primaryContainer = MedicalBlueDark,
    onPrimaryContainer = MedicalBlueLight,
    secondary = HealthGreenLight,
    onSecondary = HealthGreenDark,
    secondaryContainer = HealthGreenDark,
    onSecondaryContainer = HealthGreenLight,
    background = DarkBackground,
    onBackground = Color(0xFFE0E0E0),
    surface = DarkSurface,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = DarkCard,
    error = Color(0xFFEF9A9A)
)

@Composable
fun VitaScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VitaScanTypography,
        content = content
    )
}
