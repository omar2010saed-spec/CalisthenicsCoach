package com.calisthenics.coach.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val Green = Color(0xFF22C55E)
val GreenDark = Color(0xFF16A34A)
val Amber = Color(0xFFF59E0B)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val RedSoft = Color(0xFFEF4444)

private val DarkColors = darkColorScheme(
    primary = Green,
    onPrimary = Slate900,
    secondary = Amber,
    onSecondary = Slate900,
    background = Slate900,
    onBackground = Color(0xFFE2E8F0),
    surface = Slate800,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Slate700,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = RedSoft,
    outline = Color(0xFF64748B)
)

private val LightColors = lightColorScheme(
    primary = GreenDark,
    onPrimary = Color.White,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    error = Color(0xFFDC2626),
    outline = Color(0xFF94A3B8)
)

/** theme: 0 = follow system, 1 = light, 2 = dark. The whole app is forced to right-to-left. */
@Composable
fun CoachTheme(theme: Int, content: @Composable () -> Unit) {
    val dark = when (theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
