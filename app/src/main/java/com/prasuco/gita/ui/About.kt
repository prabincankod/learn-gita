package com.prasuco.gita.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository

// ---------- about: mission, data source, privacy, feedback ----------

private const val GITA_DATA_URL = "https://github.com/gita/gita"
private const val ISSUES_URL = "https://github.com/prabincankod/learn-gita/issues"

@Composable
fun AboutScreen(repo: Repository, onBack: () -> Unit) {
    val ctx = LocalContext.current
    fun open(url: String) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) { }
    }
    val version = remember {
        try {
            val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
            "${info.versionName} ($code)"
        } catch (_: Exception) { "" }
    }
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            TopBarBack("About", null, onBack)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Eyebrow("Mission")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "One verse every morning, in plain English. No pressure, no grind — a quiet daily companion for the Bhagavad Gita.",
                            fontSize = 15.sp, color = Ink, lineHeight = 23.sp
                        )
                    }
                }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Eyebrow("How to use", Ink2)
                        Spacer(Modifier.height(8.dp))
                        Text("• Read today's verse on the Today tab", fontSize = 14.sp, color = Ink, modifier = Modifier.padding(vertical = 2.dp))
                        Text("• Browse all 18 chapters in Chapters", fontSize = 14.sp, color = Ink, modifier = Modifier.padding(vertical = 2.dp))
                        Text("• Ask what you're facing in Search", fontSize = 14.sp, color = Ink, modifier = Modifier.padding(vertical = 2.dp))
                        Text("• Save verses and verse cards for later", fontSize = 14.sp, color = Ink, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Eyebrow("Gita data from", Ink2)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Verse text, translations and commentaries derive from the open gita/gita dataset, curated and shortened for daily reading.",
                            fontSize = 14.sp, color = Ink, lineHeight = 21.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { open(GITA_DATA_URL) }, contentPadding = PaddingValues(0.dp)) {
                            Text("github.com/gita/gita", color = DeepGold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "English: Shri Purohit Swami, Swami Sivananda · Commentary: Swami Sivananda, Swami Ramsukhdas",
                            fontSize = 13.sp, color = Ink2, lineHeight = 19.sp
                        )
                    }
                }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Eyebrow("Privacy", Ink2)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Everything stays on your device. No account, no analytics, no network calls — your progress, bookmarks and notes never leave your phone.",
                            fontSize = 14.sp, color = Ink, lineHeight = 21.sp
                        )
                    }
                }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Eyebrow("Feedback", Ink2)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Found a wrong verse or want a feature? We read every message.",
                            fontSize = 14.sp, color = Ink, lineHeight = 21.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        DuoButton("Report on GitHub", { open(ISSUES_URL) })
                    }
                }
                if (version.isNotBlank()) {
                    item {
                        Text(
                            "Learn Gita $version",
                            fontSize = 12.sp, color = Ink2,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
