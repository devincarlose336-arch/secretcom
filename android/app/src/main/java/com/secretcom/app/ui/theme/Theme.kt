package com.secretcom.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Primary = Color(0xFF1B2838)
val PrimaryVariant = Color(0xFF0D1B2A)
val Secondary = Color(0xFF00B4D8)
val Background = Color(0xFF0D1B2A)
val Surface = Color(0xFF1B2838)
val SurfaceVariant = Color(0xFF243447)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFFFFFFFF)
val OnSurface = Color(0xFFFFFFFF)
val Error = Color(0xFFCF6679)
val Accent = Color(0xFF48BB78)
val PttActive = Color(0xFF48BB78)
val PttInactive = Color(0xFF2D3748)
val CardBackground = Color(0xFF1E3A5F)

private val DarkColorScheme = darkColorScheme(
    primary = Secondary,
    onPrimary = OnPrimary,
    secondary = Accent,
    onSecondary = OnSecondary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    error = Error,
)

private val LightColorScheme = lightColorScheme(
    primary = Secondary,
    onPrimary = OnPrimary,
    secondary = Accent,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    error = Error,
)

@Composable
fun SecretcomTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PrimaryVariant.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
