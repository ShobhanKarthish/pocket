package com.shobhankarthish.pocket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = LightPrimaryText,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightPrimaryText,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = LightSecondaryText,
    onSecondary = Color(0xFFFFFFFF),
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightContainer,
    onSurface = LightPrimaryText,
    surfaceVariant = LightPlaceholder,
    onSurfaceVariant = LightSecondaryText,
    outline = LightOutline,
    outlineVariant = LightDivider,
    inverseSurface = DarkBackground,
    inverseOnSurface = DarkPrimaryText,
    inversePrimary = DarkPrimaryText,
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimaryText,
    onPrimary = LightPrimaryText,
    primaryContainer = DarkPrimaryText,
    onPrimaryContainer = LightPrimaryText,
    secondary = DarkSecondaryText,
    onSecondary = LightPrimaryText,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkContainer,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkPlaceholder,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    inverseSurface = LightBackground,
    inverseOnSurface = LightPrimaryText,
    inversePrimary = LightPrimaryText,
)

@Composable
fun PocketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = PocketTypography,
        content = content,
    )
}
