package com.prasuco.gita

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prasuco.gita.data.GitaDatabase
import com.prasuco.gita.data.Prefs
import com.prasuco.gita.data.Repository
import com.prasuco.gita.work.ReminderScheduler
import com.prasuco.gita.work.WidgetRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GitaApp : Application() {
    val db by lazy { GitaDatabase.get(this) }
    val prefs by lazy { Prefs(this) }
    val repo by lazy { Repository(db, prefs) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            try {
                repo.ensureSeeded(this@GitaApp)
            } catch (_: Exception) { }
            ReminderScheduler.reschedule(this@GitaApp, prefs)
        }
        val req = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(1, TimeUnit.HOURS)
            .addTag("widget-refresh")
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget-refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }
}
