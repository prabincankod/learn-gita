package com.prasuco.gita.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository
import kotlinx.coroutines.launch

// ---------- onboarding: calm, two steps ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(repo: Repository, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var remind by remember { mutableStateOf(true) }
    var hh by remember { mutableIntStateOf(7) }
    var mm by remember { mutableIntStateOf(0) }
    var pickTime by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            LinearProgressIndicator(
                progress = { (step + 1) / 2f }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(8.dp)),
                color = Gold, trackColor = Divider
            )
            Spacer(Modifier.height(24.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                when (step) {
                    0 -> {
                        Box(Modifier.size(96.dp).clip(CircleShape).background(GoldSoft).border(BorderStroke(2.dp, Gold), CircleShape), contentAlignment = Alignment.Center) {
                            Text("🪷", fontSize = 44.sp, color = DeepGold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("A verse a day", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("One slok each morning, in Krishna's own words.\nNo pressure, no grind — just read.", fontSize = 15.sp, color = Ink2, textAlign = TextAlign.Center)
                    }
                    1 -> {
                        Text("A gentle nudge?", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("One quiet notification each morning with the day's verse.", fontSize = 14.sp, color = Ink2, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        OptionCard(if (remind) "Daily verse · ON" else "Daily verse · OFF", "${fmtTime(hh, mm)} · local only", remind) { remind = !remind }
                        Spacer(Modifier.height(8.dp))
                        OptionCard("Reminder time · ${fmtTime(hh, mm)}", "Tap to change", false) { pickTime = true }
                    }
                }
            }
            if (step < 1) {
                DuoButton("Continue", { step++ })
            } else {
                DuoButton(if (remind) "Set Reminder & Start" else "Begin", {
                    scope.launch {
                        repo.prefs.setReminder(remind, hh, mm); repo.prefs.setOnboarded(); onDone()
                    }
                })
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = {
                        scope.launch {
                            repo.prefs.setReminder(false, 7, 0); repo.prefs.setOnboarded(); onDone()
                        }
                    }) { Text("Skip for now", color = Muted) }
                }
            }
            if (pickTime) ReminderTimeDialog(hh, mm, { pickTime = false }, { h, m -> hh = h; mm = m; pickTime = false })
        }
    }
}
