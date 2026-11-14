package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TealOcean,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFD0F4FA),
    onPrimaryContainer = TealDeep,
    secondary = EmeraldVibrant,
    onSecondary = PureWhite,
    secondaryContainer = MatchedGreenBg,
    onSecondaryContainer = ForestDark,
    tertiary = CyanVibrant,
    onTertiary = PureWhite,
    background = FrostSurface,
    onBackground = SlateInk,
    surface = PureWhite,
    onSurface = SlateInk,
    surfaceVariant = Color(0xFFE6F4F1),
    onSurfaceVariant = SlateMuted,
    outline = DividerLight,
    error = MissingRed,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = CyanVibrant,
    onPrimary = ForestDark,
    primaryContainer = TealDeep,
    onPrimaryContainer = Color(0xFFBDF2FC),
    secondary = MintGlow,
    onSecondary = ForestDark,
    secondaryContainer = EmeraldDeep,
    onSecondaryContainer = MatchedGreenBg,
    tertiary = EmeraldVibrant,
    onTertiary = ForestDark,
    background = DarkTealSurface,
    onBackground = PureWhite,
    surface = DarkTealCard,
    onSurface = PureWhite,
    surfaceVariant = DarkTealElevated,
    onSurfaceVariant = Color(0xFFB4D5D9),
    outline = Color(0xFF28555C),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun InvoiceAuditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
