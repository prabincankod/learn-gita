package com.prasuco.gita.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/** Single resizable verse widget: 2x2 compact, 4x2 hero. Tap opens the verse. */
class VerseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        WidgetUpdater.refreshAll(ctx)
    }

    override fun onAppWidgetOptionsChanged(ctx: Context, mgr: AppWidgetManager, id: Int, opts: Bundle) {
        WidgetUpdater.refreshAll(ctx)
    }
}
