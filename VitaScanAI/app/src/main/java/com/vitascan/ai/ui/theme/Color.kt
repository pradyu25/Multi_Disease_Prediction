package com.vitascan.ai.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════
// BASE: Monochromatic Black & White Palette (app chrome, backgrounds, text)
// ═══════════════════════════════════════════════════════════════════════════
val PureBlack    = Color(0xFF000000)
val NearBlack    = Color(0xFF121212)
val CharcoalGray = Color(0xFF1F1F1F)
val DeepGray     = Color(0xFF2D2D2D)
val MediumGray   = Color(0xFF757575)
val LightGray    = Color(0xFFE0E0E0)
val GhostWhite   = Color(0xFFF8F9FA)
val PureWhite    = Color(0xFFFFFFFF)

// Accent shades (monochromatic)
val BorderGray    = Color(0xFFEBEBEB)
val ElevationGray = Color(0xFFF2F2F2)

// Themed aliases
val PrimaryDark     = PureBlack
val PrimaryAccent   = PureBlack
val SurfaceWhite    = PureWhite
val SurfaceCard     = PureWhite
val BackgroundLight = Color(0xFFFBFBFB)

// Dark theme specific
val DarkBackground = Color(0xFF000000)
val DarkSurface    = Color(0xFF121212)
val DarkCard       = Color(0xFF1C1C1E)
val DarkBorder     = Color(0xFF2C2C2E)

// ═══════════════════════════════════════════════════════════════════════════
// FUNCTIONAL: Real colors for semantic meaning (buttons, risks, status)
// ═══════════════════════════════════════════════════════════════════════════

// Action Blue — primary action buttons, links, progress
val AccentBlue        = Color(0xFF1565C0)
val AccentBlueDark    = Color(0xFF003C8F)
val AccentBlueSurface = Color(0xFFE8F0FE)

// Success Green — file selected, "improved" status, positive actions
val AccentGreen        = Color(0xFF2E7D32)
val AccentGreenDark    = Color(0xFF1B5E20)
val AccentGreenSurface = Color(0xFFE8F5E9)

// Risk colors — disease predictions, warnings
val RiskHigh   = Color(0xFFD32F2F)   // Red — high risk
val RiskMedium = Color(0xFFF57C00)   // Orange — moderate risk
val RiskLow    = Color(0xFF388E3C)   // Green — low risk

// Neutral text (for captions, hints — NOT for body text)
val CaptionGray = Color(0xFF616161)

// ═══════════════════════════════════════════════════════════════════════════
// BACKWARD COMPAT: Old names → new functional colors
// ═══════════════════════════════════════════════════════════════════════════
val MedicalBlue        = AccentBlue
val MedicalBlueDark    = AccentBlueDark
val MedicalBlueSurface = AccentBlueSurface
val HealthGreen        = AccentGreen
val HealthGreenDark    = AccentGreenDark
val HealthGreenSurface = AccentGreenSurface
val NeutralGray        = CaptionGray
val NeutralGrayLight   = ElevationGray
val NeutralGrayDark    = DeepGray
val MedicalBlueLight   = Color(0xFF5E92F3)
val HealthGreenLight   = Color(0xFF60AD5E)
