package com.prasuco.gita.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.widget.RemoteViews
import com.prasuco.gita.GitaApp
import com.prasuco.gita.R
import com.prasuco.gita.data.Verse
import com.prasuco.gita.data.dailySaying
import com.prasuco.gita.ui.ChapterArt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Single resizable verse widget.
 * Narrow (< 260dp) → compact white card. Wide → gradient hero art.
 * Tap anywhere opens the full verse.
 */
object WidgetUpdater {
    private const val WIDE_MIN_DP = 260

    fun refreshAll(ctx: Context) {
        val app = ctx.applicationContext as? GitaApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try { app.repo.ensureSeeded(ctx) } catch (_: Exception) {}
            val pair = try { app.repo.dailySaying(LocalDate.now()) } catch (_: Exception) { null }
            val mgr = AppWidgetManager.getInstance(ctx)
            mgr.getAppWidgetIds(ComponentName(ctx, VerseWidgetProvider::class.java)).forEach { id ->
                val rv = if (isWide(mgr, id)) wideViews(ctx, pair) else compactViews(ctx, pair)
                pair?.let { (v, _) ->
                    rv.setOnClickPendingIntent(R.id.widget_root, deepLink(ctx, "gita://verse/${v.chapter}/${v.verse}", id))
                }
                mgr.updateAppWidget(id, rv)
            }
        }
    }

    private fun isWide(mgr: AppWidgetManager, id: Int): Boolean = try {
        mgr.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 999) >= WIDE_MIN_DP
    } catch (_: Exception) {
        true
    }

    private fun compactViews(ctx: Context, pair: Pair<Verse, String>?): RemoteViews {
        val rv = RemoteViews(ctx.packageName, R.layout.widget_verse)
        pair?.let { (v, saying) ->
            rv.setTextViewText(R.id.widget_verse_text, saying.ifBlank { "A verse for today" })
            rv.setTextViewText(R.id.widget_verse_ref, "Krishna · ${v.chapter}.${v.verse}")
        }
        return rv
    }

    private fun wideViews(ctx: Context, pair: Pair<Verse, String>?): RemoteViews {
        val rv = RemoteViews(ctx.packageName, R.layout.widget_verse_wide)
        pair?.let { (v, saying) ->
            rv.setImageViewBitmap(R.id.widget_hero_art, heroBitmap(ctx, v.chapter))
            rv.setTextViewText(R.id.widget_hero_ref, "${v.chapter}.${v.verse}")
            rv.setTextViewText(R.id.widget_hero_text, saying.ifBlank { "A verse for today" })
        }
        return rv
    }

    /** Rounded-corner gradient (RemoteViews can't clip, so round the bitmap itself). */
    private fun heroBitmap(ctx: Context, chapter: Int): Bitmap {
        val w = 640
        val h = 360
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            ChapterArt.bgInt(chapter), ChapterArt.deepInt(chapter),
            Shader.TileMode.CLAMP
        )
        val r = 24f * ctx.resources.displayMetrics.density
        canvas.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, paint)
        return bmp
    }

    private fun deepLink(ctx: Context, uri: String, req: Int): PendingIntent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            `package` = ctx.packageName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(ctx, req, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
