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
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = ElevationGray,
    onPrimaryContainer = PureBlack,
    secondary = AccentBlue,
    onSecondary = PureWhite,
    secondaryContainer = AccentBlueSurface,
    onSecondaryContainer = AccentBlueDark,
    tertiary = AccentGreen,
    onTertiary = PureWhite,
    tertiaryContainer = AccentGreenSurface,
    onTertiaryContainer = AccentGreenDark,
    background = GhostWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = GhostWhite,
    onSurfaceVariant = DeepGray,
    error = RiskHigh,
    errorContainer = Color(0xFFFFEBEE),
    onError = PureWhite,
    onErrorContainer = RiskHigh,
    outline = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = DeepGray,
    onPrimaryContainer = PureWhite,
    secondary = MedicalBlueLight,
    onSecondary = PureBlack,
    secondaryContainer = AccentBlueDark,
    onSecondaryContainer = MedicalBlueLight,
    tertiary = HealthGreenLight,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = LightGray,
    surface = NearBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = LightGray,
    error = Color(0xFFEF9A9A),
    errorContainer = Color(0xFF93000A),
    onError = PureBlack,
    onErrorContainer = Color(0xFFFFDAD6)
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
