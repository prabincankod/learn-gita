package com.prasuco.gita.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

// Crisp modern system: slate canvas, white cards with soft ambient shadow,
// amber accent. No hard container borders.
val Bg = Color(0xFFF8F9FA)
val Surface = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFE9ECEF)
val Gold = Color(0xFFD97706)
val DeepGold = Color(0xFF1E293B)
val GoldSoft = Color(0xFFFEF3C7)
val AmberInk = Color(0xFF78350D)
val Pill = Color(0xFFF1F3F5)
val Ink = Color(0xFF0F172A)
val Ink2 = Color(0xFF64748B)
val Muted = Color(0xFF94A3B8)
val Divider = Color(0xFFE9ECEF)
val Success = Color(0xFF58CC02)
val SuccessDark = Color(0xFF46A302)
val Locked = Color(0xFFE5E5E5)
val LockedInk = Color(0xFFAFAFAF)
val Wrong = Color(0xFFFF4B4B)

private val Scheme = lightColorScheme(
    primary = DeepGold,
    onPrimary = Color.White,
    secondary = Gold,
    background = Bg,
    surface = Surface,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Divider,
    onSurfaceVariant = Ink2
)

private val Type = Typography(
    headlineLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink),
    bodyLarge = TextStyle(fontSize = 16.sp, color = Ink, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, color = Ink2, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
)

@Composable
fun GitaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = Type, content = content)
}

/** White card with soft diffused ambient shadow — no hard borders. */
fun Modifier.modernCard(
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    bg: Color = Color.White
): Modifier =
    shadow(10.dp, shape, ambientColor = Color(0x120F172A), spotColor = Color(0x120F172A))
        .background(bg, shape)
