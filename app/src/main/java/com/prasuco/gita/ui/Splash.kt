package com.prasuco.gita.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.dailySaying
import kotlinx.coroutines.delay

// ---------- splash: cold-start gate + prewarm ----------

@Composable
fun SplashScreen(repo: Repository, target: String?, onReady: (String) -> Unit) {
    val ctx = LocalContext.current
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        if (shown) 1f else 0.6f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "logo"
    )
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        val t0 = android.os.SystemClock.uptimeMillis()
        try {
            // major checks: seeded DB, today's verse computable, prefs readable
            repo.ensureSeeded(ctx)
            repo.dailySaying()
            repo.prefs.streak()
        } catch (_: Exception) { }
        val wait = 1000L - (android.os.SystemClock.uptimeMillis() - t0)
        if (wait > 0) delay(wait)
        onReady(target)
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFF0F172A)) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF7C2D12), Color(0xFFF59E0B)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                        .size(112.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) { Text("🪷", fontSize = 56.sp) }
                Spacer(Modifier.height(16.dp))
                Text("Learn Gita", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text("a verse every morning", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(28.dp))
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.85f), strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
        }
    }
}
