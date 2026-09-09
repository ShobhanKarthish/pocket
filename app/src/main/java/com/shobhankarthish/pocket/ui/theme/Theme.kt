package com.shobhankarthish.pocket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = LightPrimaryText,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightPrimaryText,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = LightSecondaryText,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LightPlaceholder,
    onSecondaryContainer = LightPrimaryText,
    tertiary = LightSecondaryText,
    onTertiary = Color.White,
    tertiaryContainer = LightPlaceholder,
    onTertiaryContainer = LightPrimaryText,
    error = LightPrimaryText,
    onError = Color.White,
    errorContainer = LightPlaceholder,
    onErrorContainer = LightPrimaryText,
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
    surfaceTint = LightPrimaryText,
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE0E0E0),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightContainer,
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = LightPlaceholder,
    surfaceContainerHighest = Color(0xFFE8E8E8),
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimaryText,
    onPrimary = LightPrimaryText,
    primaryContainer = DarkPrimaryText,
    onPrimaryContainer = LightPrimaryText,
    secondary = DarkSecondaryText,
    onSecondary = LightPrimaryText,
    secondaryContainer = DarkPlaceholder,
    onSecondaryContainer = DarkPrimaryText,
    tertiary = DarkSecondaryText,
    onTertiary = LightPrimaryText,
    tertiaryContainer = DarkPlaceholder,
    onTertiaryContainer = DarkPrimaryText,
    error = DarkPrimaryText,
    onError = LightPrimaryText,
    errorContainer = DarkPlaceholder,
    onErrorContainer = DarkPrimaryText,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkContainer,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkPlaceholder,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkOutline,
    outlineVariant = DarkDivider,
    inverseSurface = LightBackground,
    inverseOnSurface = LightPrimaryText,
    inversePrimary = LightPrimaryText,
    surfaceTint = DarkPrimaryText,
    scrim = Color.Black,
    surfaceBright = Color(0xFF383838),
    surfaceDim = DarkBackground,
    surfaceContainerLowest = Color(0xFF0C0C0C),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = Color(0xFF242424),
    surfaceContainerHighest = DarkDivider,
)

private val ControlShape = RoundedCornerShape(Astra.RadiusDp.dp)
private val PocketShapes = Shapes(
    extraSmall = ControlShape,
    small = ControlShape,
    medium = ControlShape,
    large = ControlShape,
    extraLarge = ControlShape,
)

@Composable
fun PocketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = PocketTypography,
        shapes = PocketShapes,
        content = content,
    )
}
