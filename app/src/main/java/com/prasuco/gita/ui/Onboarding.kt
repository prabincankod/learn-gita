package com.prasuco.gita.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository
import kotlinx.coroutines.launch

// ---------- onboarding: three intro pages + reminder ----------

private data class IntroPage(
    val title: String,
    val sub: String,
    val emoji: String,
    val top: Color,
    val bottom: Color
)

private val INTROS = listOf(
    IntroPage(
        "A verse every morning",
        "One saying from Krishna, same for everyone.\nThirty seconds, no pressure, no grind.",
        "🪷", Color(0xFF7C2D12), Color(0xFFFDE68A)
    ),
    IntroPage(
        "Wisdom for hard days",
        "Anxious? Stuck? Grieving? Ask what you're\nfacing and get verses that speak to it.",
        "🧭", Color(0xFF312E81), Color(0xFFA5B4FC)
    ),
    IntroPage(
        "Carry it with you",
        "A home-screen widget, a morning nudge,\nand verse cards saved to your gallery.",
        "🖼️", Color(0xFF0F172A), Color(0xFF64748B)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(repo: Repository, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var remind by remember { mutableStateOf(true) }
    var hh by remember { mutableIntStateOf(7) }
    var mm by remember { mutableIntStateOf(0) }
    var pickTime by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val total = INTROS.size + 1
    val progress by animateFloatAsState((step + 1) / total.toFloat(), tween(400), label = "progress")

    fun finish(withReminder: Boolean) {
        scope.launch {
            if (withReminder) repo.prefs.setReminder(remind, hh, mm)
            else repo.prefs.setReminder(false, 7, 0)
            repo.prefs.setOnboarded()
            onDone()
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            LinearProgressIndicator(
                progress = { progress }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(8.dp)),
                color = Gold, trackColor = Divider
            )
            Spacer(Modifier.height(24.dp))
            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn(tween(350))).togetherWith(
                        slideOutHorizontally { -it / 3 } + fadeOut(tween(250))
                    )
                },
                label = "steps"
            ) { s ->
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (s < INTROS.size) {
                        val page = INTROS[s]
                        PlaceholderArt(page.emoji, page.top, page.bottom)
                        Spacer(Modifier.height(20.dp))
                        Text(page.title, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text(page.sub, fontSize = 15.sp, color = Ink2, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    } else {
                        ReminderStep(remind, hh, mm, { remind = !remind }, { pickTime = true })
                    }
                }
            }
            if (step < INTROS.size) {
                DuoButton("Continue", { step++ })
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { finish(false) }) { Text("Skip", color = Muted) }
                }
            } else {
                DuoButton(if (remind) "Set Reminder & Start" else "Begin", { finish(true) })
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { finish(false) }) { Text("Skip for now", color = Muted) }
                }
            }
            if (pickTime) ReminderTimeDialog(hh, mm, { pickTime = false }, { h, m -> hh = h; mm = m; pickTime = false })
        }
    }
}

/** Placeholder art until real illustrations land: gradient card + glyph. */
@Composable
fun PlaceholderArt(emoji: String, top: Color, bottom: Color) {
    val drift = rememberInfiniteTransition(label = "art")
    val float by drift.animateFloat(0f, 1f, infiniteRepeatable(tween(3600), RepeatMode.Reverse), label = "float")
    Box(
        Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(top, bottom))),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.offset(y = (6 - float * 12).dp),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 88.sp) }
    }
}

@Composable
fun ReminderStep(remind: Boolean, hh: Int, mm: Int, onToggle: () -> Unit, onTime: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("A gentle nudge?", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text("One quiet notification each morning with the day's verse.", fontSize = 14.sp, color = Ink2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OptionCard(if (remind) "Daily verse · ON" else "Daily verse · OFF", "${fmtTime(hh, mm)} · local only", remind) { onToggle() }
        Spacer(Modifier.height(8.dp))
        OptionCard("Reminder time · ${fmtTime(hh, mm)}", "Tap to change", false) { onTime() }
    }
}
