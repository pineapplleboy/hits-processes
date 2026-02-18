package com.example.googleclass.common.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,

    secondary = DarkGray,
    onSecondary = White,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    outline = Outline,

    error = ErrorRed,
    onError = White
)

@Composable
fun GoogleClassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BankTypography,
        content = content
    )
}
