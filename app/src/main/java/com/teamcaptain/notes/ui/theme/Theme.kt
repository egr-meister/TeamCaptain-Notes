package com.teamcaptain.notes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Green-Blue Captain Board palette ---
val CaptainGreen = Color(0xFF1F7A4D)
val CaptainGreenDeep = Color(0xFF17633E)
val CaptainBlue = Color(0xFF2E3F8F)
val CaptainBlueDeep = Color(0xFF243477)
val SoftGreenPanel = Color(0xFFE6F4EC)
val SoftBluePanel = Color(0xFFE9EDFB)

val DeepNavy = Color(0xFF1F2937)
val DarkCharcoal = Color(0xFF111827)
val DarkSection = Color(0xFF16202A)

val AppBackground = Color(0xFFF7F8F6)
val WhiteCard = Color(0xFFFFFFFF)
val SoftMixedBackground = Color(0xFFF1F5F9)

val CardDarkText = Color(0xFF1F1F1F)
val SecondaryGray = Color(0xFF6B7280)
val MutedGray = Color(0xFF9CA3AF)

val SuccessGreen = Color(0xFF22C55E)
val WarningYellow = Color(0xFFFACC15)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

private val LightColors = lightColorScheme(
    primary = CaptainGreen,
    onPrimary = Color.White,
    primaryContainer = SoftGreenPanel,
    onPrimaryContainer = CaptainGreenDeep,
    secondary = CaptainBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftBluePanel,
    onSecondaryContainer = CaptainBlueDeep,
    tertiary = InfoBlue,
    background = AppBackground,
    onBackground = CardDarkText,
    surface = WhiteCard,
    onSurface = CardDarkText,
    surfaceVariant = SoftMixedBackground,
    onSurfaceVariant = SecondaryGray,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFD5DAE0)
)

private val DarkColors = darkColorScheme(
    primary = SuccessGreen,
    onPrimary = DarkCharcoal,
    primaryContainer = CaptainGreenDeep,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF8FA0F0),
    onSecondary = DarkCharcoal,
    secondaryContainer = CaptainBlueDeep,
    onSecondaryContainer = Color.White,
    background = DarkCharcoal,
    onBackground = Color(0xFFECEFF3),
    surface = DarkSection,
    onSurface = Color(0xFFECEFF3),
    surfaceVariant = DeepNavy,
    onSurfaceVariant = Color(0xFFB6BEC9),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF3A4655)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

@Composable
fun TeamCaptainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
