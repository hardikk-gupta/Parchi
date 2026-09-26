package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ParchiPurpleSupporting, // #A500FF
    onPrimary = Color.White,
    primaryContainer = ParchiPurpleContainer, // #F0D6FF
    onPrimaryContainer = ParchiPurpleDark,
    secondary = ParchiPurplePrimary, // #E7BBFF
    onSecondary = ParchiPurpleDark,
    secondaryContainer = ParchiPurpleLight,
    onSecondaryContainer = ParchiPurpleDark,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = CanvasBackground,
    onBackground = InkDark,
    surface = SurfaceCard,
    onSurface = InkDark,
    surfaceVariant = Color(0xFFF8F5FC),
    onSurfaceVariant = InkMuted,
    outline = InkBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
