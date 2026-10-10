package com.prasuco.gita.share

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.prasuco.gita.ui.ChapterArt

/** On-the-fly verse card studio: Canvas-rendered 1:1 and 9:16 share images. */
object ShareCards {

    enum class Format { SQUARE, STORY }

    enum class Preset { CLASSIC, MINIMAL, SAFFRON, ABSTRACT, SUNSET }

    enum class Decor(val glyph: String) { NONE(""), FEATHER("🪶"), LOTUS("🪷"), SUN("☀"), DIYA("🪔") }

    private data class Style(val top: Int, val bottom: Int, val darkText: Boolean)

    private fun styleFor(preset: Preset, chapter: Int): Style = when (preset) {
        Preset.CLASSIC -> Style(ChapterArt.bgInt(chapter), ChapterArt.deepInt(chapter), false)
        Preset.MINIMAL -> Style(0xFFFFFDF7.toInt(), 0xFFF3EAD8.toInt(), true)
        Preset.SAFFRON -> Style(0xFFF97316.toInt(), 0xFF7C2D12.toInt(), false)
        Preset.ABSTRACT -> Style(0xFF475569.toInt(), 0xFF0F172A.toInt(), false)
        Preset.SUNSET -> Style(0, 0, true) // painted procedurally below
    }

    /** Warm sunset sky: layered gradient, glowing sun, haze bands, corner feathers. */
    private fun drawSunsetBg(c: Canvas, w: Int, h: Int) {
        val sky = Paint(Paint.ANTI_ALIAS_FLAG)
        sky.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                0xFF4A1E0B.toInt(), 0xFFB45309.toInt(), 0xFFF59E0B.toInt(),
                0xFFFDE68A.toInt(), 0xFFF59E0B.toInt(), 0xFF9A3412.toInt()
            ),
            floatArrayOf(0f, 0.30f, 0.50f, 0.62f, 0.80f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), sky)

        // sun + halo, low on the horizon
        val sunX = w / 2f
        val sunY = h * 0.68f
        val sunR = w * 0.085f
        val halo = Paint(Paint.ANTI_ALIAS_FLAG)
        halo.shader = RadialGradient(
            sunX, sunY, sunR * 3.2f,
            intArrayOf(
                android.graphics.Color.argb(150, 255, 250, 220),
                android.graphics.Color.argb(0, 255, 250, 220)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawCircle(sunX, sunY, sunR * 3.2f, halo)
        val sun = Paint(Paint.ANTI_ALIAS_FLAG)
        sun.color = android.graphics.Color.rgb(0xFF, 0xF9, 0xDB)
        c.drawCircle(sunX, sunY, sunR, sun)

        // thin haze bands
        val haze = Paint(Paint.ANTI_ALIAS_FLAG)
        haze.color = android.graphics.Color.argb(46, 255, 255, 255)
        val bandH = h * 0.022f
        c.drawRect(0f, h * 0.52f, w.toFloat(), h * 0.52f + bandH, haze)
        c.drawRect(0f, h * 0.585f, w.toFloat(), h * 0.585f + bandH * 0.7f, haze)

        // peacock feathers tucked into three corners
        val len = w * 0.62f
        drawFeather(c, -w * 0.02f, h * 0.02f, len, -28f)
        drawFeather(c, w * 1.02f, h * 0.02f, len, 28f)
        drawFeather(c, w * 1.04f, h * 0.99f, len * 0.9f, 152f)
    }

    /** Stylized peacock feather: stem, fanned barbs, layered eye. */
    private fun drawFeather(c: Canvas, cx: Float, cy: Float, len: Float, angleDeg: Float) {
        c.save()
        c.translate(cx, cy)
        c.rotate(angleDeg)
        val stem = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(0x6B, 0x4A, 0x2A)
            strokeWidth = len * 0.016f
            style = Paint.Style.STROKE
        }
        c.drawLine(0f, len * 0.5f, 0f, -len * 0.5f, stem)
        // barbs fan out from the stem, gold low to green high
        val barb = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = len * 0.011f
            strokeCap = Paint.Cap.ROUND
        }
        for (i in -9..9) {
            val t = i / 9f
            val y0 = t * len * 0.30f
            val spread = len * 0.15f * (1 - kotlin.math.abs(t) * 0.5f)
            val droop = len * 0.045f
            val g = (0x7A + (0x2E - 0x7A) * kotlin.math.abs(t)).toInt()
            barb.color = android.graphics.Color.argb(205, 0x8A, g, 0x3C)
            c.drawLine(0f, y0, -spread, y0 - droop, barb)
            c.drawLine(0f, y0, spread, y0 - droop, barb)
        }
        // the eye: gold rim, emerald, teal, deep-blue heart
        val ey = -len * 0.30f
        fun oval(fw: Float, fh: Float, color: Int) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
            c.drawOval(RectF(-fw, ey - fh, fw, ey + fh), p)
        }
        oval(len * 0.170f, len * 0.220f, android.graphics.Color.rgb(0xC9, 0xA2, 0x27))
        oval(len * 0.128f, len * 0.168f, android.graphics.Color.rgb(0x2F, 0x7D, 0x4F))
        oval(len * 0.088f, len * 0.118f, android.graphics.Color.rgb(0x1D, 0x6F, 0x8C))
        oval(len * 0.058f, len * 0.082f, android.graphics.Color.rgb(0x2B, 0x3A, 0x9B))
        val glint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(220, 255, 255, 255)
        }
        c.drawCircle(len * 0.020f, ey - len * 0.030f, len * 0.012f, glint)
        c.restore()
    }

    fun render(
        ctx: Context,
        chapter: Int,
        ref: String,
        saying: String,
        format: Format,
        preset: Preset,
        photo: Bitmap?,
        decor: Decor,
        centered: Boolean,
        serif: Boolean
    ): Bitmap {
        val density = ctx.resources.displayMetrics.density
        val (w, h) = if (format == Format.SQUARE) 1080 to 1080 else 1080 to 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val style = styleFor(preset, chapter)

        if (preset == Preset.SUNSET && photo == null) {
            drawSunsetBg(c, w, h)
        } else if (photo != null) {
            // cover-crop photo + readability scrim
            val scale = maxOf(w / photo.width.toFloat(), h / photo.height.toFloat())
            val dw = photo.width * scale
            val dh = photo.height * scale
            val left = (w - dw) / 2f
            val top = (h - dh) / 2f
            val bp = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            c.drawBitmap(photo, null, RectF(left, top, left + dw, top + dh), bp)
            val scrim = Paint(Paint.ANTI_ALIAS_FLAG)
            scrim.shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                android.graphics.Color.argb(70, 0, 0, 0),
                android.graphics.Color.argb(175, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrim)
        } else {
            val bgp = Paint(Paint.ANTI_ALIAS_FLAG)
            bgp.shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                style.top, style.bottom, Shader.TileMode.CLAMP
            )
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgp)
        }

        // soft radial glow behind the text — the "beautiful" pass
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        glow.shader = RadialGradient(
            w / 2f, h * 0.30f, w * 0.75f,
            intArrayOf(
                android.graphics.Color.argb(60, 255, 255, 255),
                android.graphics.Color.argb(0, 255, 255, 255)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

        val sunset = preset == Preset.SUNSET && photo == null
        val dark = (style.darkText || preset == Preset.SUNSET) && photo == null
        val ink = when {
            photo != null -> android.graphics.Color.WHITE
            sunset -> android.graphics.Color.rgb(0x43, 0x14, 0x07)
            dark -> android.graphics.Color.rgb(0x1F, 0x29, 0x37)
            else -> android.graphics.Color.WHITE
        }
        val sub = when {
            photo != null -> android.graphics.Color.argb(225, 255, 255, 255)
            sunset -> android.graphics.Color.rgb(0x78, 0x35, 0x0D)
            dark -> android.graphics.Color.rgb(0x4B, 0x55, 0x63)
            else -> android.graphics.Color.argb(225, 255, 255, 255)
        }

        // decor sticker, top-right
        if (decor != Decor.NONE) {
            val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 235 }
            dp.textSize = 120f * density
            dp.textAlign = Paint.Align.CENTER
            if (!dark) dp.setShadowLayer(12f, 0f, 5f, android.graphics.Color.argb(110, 0, 0, 0))
            c.drawText(decor.glyph, w - 110f * density, 190f * density, dp)
        }

        val padX = w * 0.08f
        val cx = w / 2f
        var y = h * if (format == Format.SQUARE) 0.14f else 0.17f

        // eyebrow
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sub
            textSize = 30f * density
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        if (Build.VERSION.SDK_INT >= 21) labelPaint.letterSpacing = 0.18f
        val label = "GITA · DAILY VERSE"
        y += labelPaint.textSize
        val labelW = labelPaint.measureText(label)
        if (centered) {
            val lx = cx - labelW / 2f
            c.drawText(label, lx, y, labelPaint)
            // flanking hairlines
            val hair = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (dark) android.graphics.Color.argb(110, 0, 0, 0)
                else android.graphics.Color.argb(140, 255, 255, 255)
                strokeWidth = 2f * density
            }
            val gap = 18f * density
            val len = 64f * density
            val ly = y - labelPaint.textSize * 0.32f
            c.drawLine(lx - gap - len, ly, lx - gap, ly, hair)
            c.drawLine(lx + labelW + gap, ly, lx + labelW + gap + len, ly, hair)
        } else {
            c.drawText(label, padX, y, labelPaint)
        }
        y += 44f * density

        // saying, auto-fit
        val align = if (centered) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
        val maxW = if (centered) (w * 0.84f).toInt() else (w - 2 * padX).toInt()
        val maxTextH = h * if (format == Format.SQUARE) 0.42f else 0.36f
        var sizeSp = if (format == Format.SQUARE) 66f else 74f
        var layout: StaticLayout? = null
        var face: Typeface? = null
        while (sizeSp > 38f) {
            face = Typeface.create(
                if (serif) Typeface.SERIF else Typeface.SANS_SERIF,
                Typeface.NORMAL
            )
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ink
                textSize = sizeSp * density
                typeface = face
                if (!dark) setShadowLayer(14f, 0f, 5f, android.graphics.Color.argb(110, 0, 0, 0))
            }
            val sl = StaticLayout.Builder.obtain(saying, 0, saying.length, tp, maxW)
                .setAlignment(align)
                .setLineSpacing(10f * density, 1f)
                .setIncludePad(false)
                .build()
            val maxLines = if (format == Format.SQUARE) 7 else 9
            if (sl.height <= maxTextH && sl.lineCount <= maxLines) {
                layout = sl
                break
            }
            sizeSp -= 4f
        }
        layout?.let {
            c.save()
            c.translate(if (centered) 0f else padX, y)
            it.draw(c)
            c.restore()
            y += it.height + 40f * density
        }

        // ornament diamond + ref
        val gemPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dark) android.graphics.Color.argb(150, 0, 0, 0)
            else android.graphics.Color.argb(200, 255, 255, 255)
            textSize = 24f * density
            textAlign = Paint.Align.CENTER
        }
        y += gemPaint.textSize + 8f * density
        if (centered) c.drawText("◆", cx, y, gemPaint)
        else c.drawText("◆", padX + gemPaint.measureText("◆") / 2f, y, gemPaint)
        y += 30f * density
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 40f * density
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            if (!dark) setShadowLayer(10f, 0f, 4f, android.graphics.Color.argb(110, 0, 0, 0))
        }
        val refLine = "Krishna · $ref"
        y += refPaint.textSize
        if (centered) c.drawText(refLine, cx - refPaint.measureText(refLine) / 2f, y, refPaint)
        else c.drawText(refLine, padX, y, refPaint)

        // footer brand
        val footPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sub
            textSize = 26f * density
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        if (Build.VERSION.SDK_INT >= 21) footPaint.letterSpacing = 0.22f
        val foot = "A VERSE A DAY"
        val fy = h - 64f * density
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dark) android.graphics.Color.argb(90, 0, 0, 0)
            else android.graphics.Color.argb(120, 255, 255, 255)
            strokeWidth = 2f * density
        }
        val halfLine = 90f * density
        if (centered) {
            c.drawLine(cx - halfLine, fy - 34f * density, cx + halfLine, fy - 34f * density, linePaint)
            c.drawText(foot, cx - footPaint.measureText(foot) / 2f, fy, footPaint)
        } else {
            c.drawLine(padX, fy - 34f * density, padX + 2 * halfLine, fy - 34f * density, linePaint)
            c.drawText(foot, padX, fy, footPaint)
        }

        return bmp
    }

    /** Saves PNG to the gallery. Returns true on success. Call off the main thread. */
    fun saveToGallery(ctx: Context, bmp: Bitmap, ref: String, format: Format): Boolean {
        return try {
            val name = "gita-$ref-${format.name.lowercase()}"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Gita")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val resolver = ctx.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            resolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
