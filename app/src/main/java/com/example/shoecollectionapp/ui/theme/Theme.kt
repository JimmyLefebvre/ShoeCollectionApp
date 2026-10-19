package com.example.shoecollectionapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = SecondarySky,
    secondaryContainer = SecondarySkyContainer,
    onSecondaryContainer = OnSecondarySkyContainer,
    background = BlueSlateBackground,
    onBackground = TextWhite,
    surface = BlueSlateSurface,
    onSurface = TextWhite,
    surfaceVariant = BlueSlateCard,
    onSurfaceVariant = TextMuted,
    surfaceContainer = BlueSlateCard,
    surfaceContainerHigh = BlueSlateCardVariant,
    surfaceContainerHighest = BlueSlateDialog
)

private val LightColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = SecondarySky,
    secondaryContainer = SecondarySkyContainer,
    onSecondaryContainer = OnSecondarySkyContainer,
    background = BlueSlateBackground,
    onBackground = TextWhite,
    surface = BlueSlateSurface,
    onSurface = TextWhite,
    surfaceVariant = BlueSlateCard,
    onSurfaceVariant = TextMuted,
    surfaceContainer = BlueSlateCard,
    surfaceContainerHigh = BlueSlateCardVariant,
    surfaceContainerHighest = BlueSlateDialog
)

@Composable
fun ShoeCollectionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
