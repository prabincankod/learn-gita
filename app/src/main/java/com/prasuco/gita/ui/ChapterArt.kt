package com.prasuco.gita.ui

import androidx.compose.ui.graphics.Color

/**
 * Per-chapter hero art.
 *
 * V1: calm duotone gradient palettes (one per chapter). When the real
 * 18-image set lands, drop `chapter_1.png … chapter_18.png` into
 * `res/drawable-nodpi/` and switch [hasArt] on — call sites already
 * funnel through [bg]/[deep], so no UI code changes.
 */
object ChapterArt {
    const val hasArt = false

    // 18 calm palettes: top -> deep. Warm dawns, lotus, sand, dusk indigos.
    private val palettes = listOf(
        0xFFE8B04B to 0xFF8A4B1F, // 1  marigold dawn
        0xFFF3D9A4 to 0xFFB77900, // 2  sand gold
        0xFFE9C46A to 0xFF7A5C2E, // 3  harvest
        0xFFF4E3C1 to 0xFF9C6B30, // 4  parchment
        0xFFDCE9E2 to 0xFF3E6B5E, // 5  tulsi leaf
        0xFFE6D3E3 to 0xFF6B3E6B, // 6  lotus dusk
        0xFFD7E3F4 to 0xFF3E5C76, // 7  yamuna blue
        0xFFF6D6C3 to 0xFF9C4B2E, // 8  clay diya
        0xFFEAD9F7 to 0xFF5B3E8A, // 9  temple violet
        0xFFF9E7B2 to 0xFF8A6D1F, // 10 butter lamp
        0xFFD9EAD3 to 0xFF4F7D50, // 11 peepal green
        0xFFE8DCC8 to 0xFF6B5B3E, // 12 river sand
        0xFFF2C9C9 to 0xFF8A3E3E, // 13 kumkum
        0xFFDCE7F7 to 0xFF3E4B6B, // 14 night sky
        0xFFF0E2CC to 0xFF7A5C33, // 15 sesame
        0xFFE3D5F2 to 0xFF4B3E6B, // 16 dusk prayer
        0xFFD8F0F0 to 0xFF2E6B6B, // 17 ganga teal
        0xFFF7E8C9 to 0xFF6B4B1F, // 18 moksha gold
    )

    fun bg(chapter: Int): Color {
        val (top, _) = palettes[(chapter - 1).coerceIn(0, 17)]
        return Color(top)
    }

    fun deep(chapter: Int): Color {
        val (_, bottom) = palettes[(chapter - 1).coerceIn(0, 17)]
        return Color(bottom)
    }

    /** Framework color ints for RemoteViews bitmaps (widgets can't use Compose). */
    fun bgInt(chapter: Int): Int = bg(chapter).toFrameworkInt()
    fun deepInt(chapter: Int): Int = deep(chapter).toFrameworkInt()

    private fun Color.toFrameworkInt(): Int = android.graphics.Color.argb(
        255, (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
    )
}
