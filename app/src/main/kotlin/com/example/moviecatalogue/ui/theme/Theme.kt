package com.example.moviecatalogue.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── MovFlix Dark Mode Palette ──────────────────────────────────────────────
val CineRed = Color(0xFFFF5252)         // Vibrant red for dark mode
val CineRedDark = Color(0xFFE50914)     // Deep red for containers
val CineGold = Color(0xFFFFC107)        // Bright gold for accents
val CineDarkBg = Color(0xFF0A0A0A)      // Near-black background (WCAG AA compliant)
val CineSurface = Color(0xFF121212)     // Surface layer (Material Design recommendation)
val CineSurfaceVariant = Color(0xFF1E1E1E)  // Elevated surface
val CineOnSurface = Color(0xFFFFFFFF)   // Primary text - pure white for max contrast
val CineSecondary = Color(0xFFBDBDBD)   // Secondary text - improved readability
val CineTertiary = Color(0xFF64B5F6)    // Tertiary accent - light blue

// ─── Dark Color Scheme (WCAG AA Compliant) ──────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = CineRed,
    onPrimary = Color(0xFF000000),
    primaryContainer = CineRedDark,
    onPrimaryContainer = CineOnSurface,
    secondary = CineGold,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4D3D00),
    onSecondaryContainer = CineGold,
    tertiary = CineTertiary,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1A47A0),
    onTertiaryContainer = CineTertiary,
    background = CineDarkBg,
    onBackground = CineOnSurface,
    surface = CineSurface,
    onSurface = CineOnSurface,
    surfaceVariant = CineSurfaceVariant,
    onSurfaceVariant = CineSecondary,
    outline = Color(0xFF4F4F4F),
    outlineVariant = Color(0xFF3F3F3F),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF000000)
)


@Composable
fun FinalProjectTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent system bars so the dark app shows through edge-to-edge.
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            // Light (white) icons on both bars, since the app is dark-themed.
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
