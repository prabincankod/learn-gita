package com.prasuco.gita.work

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prasuco.gita.GitaApp
import com.prasuco.gita.R
import com.prasuco.gita.data.Prefs
import com.prasuco.gita.data.dailySaying
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.prasuco.gita.widget.WidgetUpdater
import java.util.Calendar

class WidgetRefreshWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        WidgetUpdater.refreshAll(applicationContext)
        return Result.success()
    }
}

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) { WidgetUpdater.refreshAll(ctx) }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = ctx.applicationContext as? GitaApp ?: return@launch
                try { app.repo.ensureSeeded(ctx) } catch (_: Exception) {}
                val (verse, saying) = try { app.repo.dailySaying() } catch (_: Exception) { null } ?: return@launch
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
                    NotificationChannel("daily", "Daily verse", NotificationManager.IMPORTANCE_DEFAULT)
                )
                val open = PendingIntent.getActivity(
                    ctx, 1, Intent(Intent.ACTION_VIEW, Uri.parse("gita://verse/${verse.chapter}/${verse.verse}")).apply { `package` = ctx.packageName },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val n = NotificationCompat.Builder(ctx, "daily")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Today's verse · ${verse.chapter}.${verse.verse}")
                    .setContentText(saying)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(saying))
                    .setContentIntent(open)
                    .setAutoCancel(true)
                    .build()
                nm.notify(7, n)
            } finally {
                pending.finish()
            }
        }
    }
}

object ReminderScheduler {
    fun reschedule(ctx: Context, prefs: Prefs) {
        // read prefs synchronously via lightweight thread; called from IO scope
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(ctx, 7, Intent(ctx, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        // Default 7:00 AM daily; full enable/disable honored in GitaApp scope via runBlocking-free path:
        // schedule is re-applied on boot + app start.
        val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
        try {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        } catch (_: SecurityException) { }
    }
}
