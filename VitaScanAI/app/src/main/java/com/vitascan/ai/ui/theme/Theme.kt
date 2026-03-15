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
    secondary = CharcoalGray,
    onSecondary = PureWhite,
    secondaryContainer = LightGray,
    onSecondaryContainer = CharcoalGray,
    tertiary = MediumGray,
    background = GhostWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = GhostWhite,
    onSurfaceVariant = MediumGray,
    error = PureBlack,
    errorContainer = Color(0xFFFEEFF0),
    outline = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = DeepGray,
    onPrimaryContainer = PureWhite,
    secondary = LightGray,
    onSecondary = PureBlack,
    secondaryContainer = CharcoalGray,
    onSecondaryContainer = LightGray,
    background = PureBlack,
    onBackground = LightGray,
    surface = NearBlack,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    error = PureWhite
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
