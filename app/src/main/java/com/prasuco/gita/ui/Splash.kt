package com.prasuco.gita.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.R
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.dailySaying
import kotlinx.coroutines.delay

// ---------- splash: static logo, cold-start gate + prewarm ----------

@Composable
fun SplashScreen(repo: Repository, target: String?, onReady: (String) -> Unit) {
    val ctx = LocalContext.current
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        val t0 = android.os.SystemClock.uptimeMillis()
        try {
            // major checks: seeded DB, today's verse computable, prefs readable
            repo.ensureSeeded(ctx)
            repo.dailySaying()
            repo.prefs.streak()
        } catch (_: Exception) { }
        val wait = 800L - (android.os.SystemClock.uptimeMillis() - t0)
        if (wait > 0) delay(wait)
        onReady(target)
    }
    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painterResource(R.mipmap.ic_launcher_foreground), null,
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(28.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text("Learn Gita", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Ink)
        }
    }
}
