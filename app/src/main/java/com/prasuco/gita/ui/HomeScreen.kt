package com.prasuco.gita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.audio.VerseSpeaker
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.Verse
import com.prasuco.gita.data.dailySaying
import com.prasuco.gita.data.dayLabel
import com.prasuco.gita.data.pastVerses
import com.prasuco.gita.data.twoLiner
import java.time.LocalDate
import kotlinx.coroutines.launch

// ---------- HOME = today's lesson ----------

@Composable
fun HomeScreen(
    repo: Repository, goVerse: (Int, Int) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val chapters by repo.chapters().collectAsState(initial = emptyList())
    var daily by remember { mutableStateOf<Verse?>(null) }
    var saying by remember { mutableStateOf("") }
    var fullTr by remember { mutableStateOf("") }
    var scholar by remember { mutableStateOf("") }
    var readCount by remember { mutableIntStateOf(0) }
    var savedToday by remember { mutableStateOf(false) }
    var streak by remember { mutableIntStateOf(0) }
    var counts by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var past by remember { mutableStateOf<List<Triple<LocalDate, Verse, String>>>(emptyList()) }
    var showSettings by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var remind by remember { mutableStateOf(true) }
    var hh by remember { mutableIntStateOf(7) }
    var mm by remember { mutableIntStateOf(0) }
    var pickTime by remember { mutableStateOf(false) }
    LaunchedEffect(chapters) {
        val dailyPair: Pair<Verse, String>? = try { repo.dailySaying() } catch (_: Exception) { null }
        daily = dailyPair?.first; saying = dailyPair?.second ?: ""
        daily?.let { d ->
            fullTr = try {
                val trs = repo.translations(d.chapter, d.verse)
                trs.firstOrNull { it.source == "Purohit_EN" }?.text ?: trs.firstOrNull()?.text ?: ""
            } catch (_: Exception) { "" }
            scholar = try {
                val cms = repo.commentaries(d.chapter, d.verse)
                val s = cms.firstOrNull { it.source == "Sivananda_EN" }?.text ?: cms.firstOrNull()?.text ?: ""
                if (s.isBlank()) "" else twoLiner(s, 320)
            } catch (_: Exception) { "" }
            savedToday = try { repo.db.users().bookmark(d.chapter, d.verse) != null } catch (_: Exception) { false }
        }
        val (remindOn, remindAt) = try { repo.prefs.reminder() } catch (_: Exception) { true to (7 to 0) }
        remind = remindOn; hh = remindAt.first; mm = remindAt.second
        readCount = try { repo.learnedCount() } catch (_: Exception) { 0 }
        streak = try { repo.prefs.streak().first } catch (_: Exception) { 0 }
        counts = chapters.associate { c ->
            c.number to try { repo.learnedInChapter(c.number) } catch (_: Exception) { 0 }
        }
        past = try {
            repo.pastVerses().map { (date, v) -> Triple(date, v, repo.dailySaying(date)?.second ?: "") }
        } catch (_: Exception) { emptyList() }
    }
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            TopBar(streak, readCount) { showSettings = true }
            daily?.let { d0 ->
                if (showSave) SaveImageSheet(d0.chapter, d0.verse, fullTr.ifBlank { saying }) { showSave = false }
            }
            if (showSettings) {
                AlertDialog(
                    onDismissRequest = { showSettings = false },
                    confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Done", color = Ink) } },
                    containerColor = Color.White,
                    title = { Text("Daily verse", fontWeight = FontWeight.Bold, color = Ink) },
                    text = {
                        Column {
                            OptionCard(
                                if (remind) "Reminder · ON" else "Reminder · OFF",
                                "${fmtTime(hh, mm)} · local only", remind
                            ) {
                                scope.launch {
                                    repo.prefs.setReminder(!remind, hh, mm); remind = !remind
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OptionCard("Time · ${fmtTime(hh, mm)}", "Tap to change", false) { pickTime = true }
                        }
                    }
                )
            }
            if (pickTime) ReminderTimeDialog(hh, mm, { pickTime = false }, { h, m ->
                scope.launch { repo.prefs.setReminder(remind, h, m); hh = h; mm = m; pickTime = false }
            })
            LazyColumn(Modifier.fillMaxSize().weight(1f), contentPadding = PaddingValues(bottom = 96.dp)) {
                // hero
                daily?.let { d ->
                    item {
                        LessonHero(
                            ch = d.chapter, ref = "${d.chapter}.${d.verse}",
                            sub = chapters.firstOrNull { it.number == d.chapter }?.nameTranslation ?: "",
                            read = counts[d.chapter] ?: 0,
                            total = chapters.firstOrNull { it.number == d.chapter }?.versesCount ?: 0
                        )
                    }
                    // verse body — English saying first
                    item {
                        AppCard(Modifier.padding(horizontal = 20.dp).padding(top = 16.dp).fillMaxWidth()) {
                            if (saying.isNotBlank()) {
                                Text("“$saying”", fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 27.sp, color = Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                            if (fullTr.isNotBlank() && fullTr != saying) {
                                Spacer(Modifier.height(8.dp))
                                Text(fullTr, fontSize = 14.sp, lineHeight = 22.sp, color = Ink2)
                            }
                        }
                    }
                    // pills: listen · save · save image
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppPill(
                                icon = if (VerseSpeaker.speaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                label = if (VerseSpeaker.speaking) "Stop" else "Listen",
                                selected = VerseSpeaker.speaking,
                                onClick = { VerseSpeaker.toggle(ctx, saying.ifBlank { fullTr }, "${d.chapter}.${d.verse}") }
                            )
                            AppPill(
                                icon = if (savedToday) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                label = if (savedToday) "Saved" else "Save",
                                selected = savedToday,
                                onClick = {
                                    scope.launch {
                                        try {
                                            if (savedToday) { repo.db.users().removeBookmark(d.chapter, d.verse); savedToday = false }
                                            else { repo.db.users().addBookmark(com.prasuco.gita.data.Bookmark(d.chapter, d.verse)); savedToday = true }
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                            AppPill(icon = Icons.Default.Download, label = "Save image", onClick = { showSave = true })
                        }
                    }
                    // primary action
                    item {
                        Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp)) {
                            DuoButton("Read full verse & commentary", { daily?.let { goVerse(it.chapter, it.verse) } })
                        }
                    }
                    // reflection
                    item {
                        ReflectCard(questions = reflectionFor(d.order))
                    }
                    // scholar take
                    if (scholar.isNotBlank()) {
                        item {
                            ScholarCard(text = scholar) { goVerse(d.chapter, d.verse) }
                        }
                    }
                }
                // past week carousel
                if (past.size > 1) {
                    item {
                        Column(Modifier.padding(top = 16.dp)) {
                            Eyebrow("Past week", Ink2, Modifier.padding(horizontal = 20.dp))
                            Spacer(Modifier.height(8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(past.drop(1), key = { "${it.second.chapter}.${it.second.verse}" }) { (date, v, s) ->
                                    Column(Modifier.width(140.dp).modernCard(RoundedCornerShape(16.dp))
                                        .clickable { goVerse(v.chapter, v.verse) }.padding(12.dp)) {
                                        Eyebrow(dayLabel(date), DeepGold)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${v.chapter}.${v.verse}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                                        Text(if (s.isNotBlank()) "“$s”" else "",
                                            fontSize = 12.sp, color = Ink2, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
                // chapters live in the Chapters tab — feed ends with past week
            }
        }
    }
}

@Composable
fun LessonHero(ch: Int, ref: String, sub: String, read: Int, total: Int) {
    Box(Modifier.padding(horizontal = 20.dp).padding(top = 16.dp).fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(Brush.verticalGradient(listOf(ChapterArt.bg(ch), ChapterArt.deep(ch))))
        .padding(24.dp)) {
        Column {
            Eyebrow("TODAY'S LESSON · CHAPTER $ch", Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(6.dp))
            Text(ref, fontWeight = FontWeight.Bold, fontSize = 38.sp, color = Color.White)
            if (sub.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(sub, fontSize = 17.sp, lineHeight = 26.sp, color = Color.White.copy(alpha = 0.92f))
            }
            if (total > 0) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { read / total.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp)),
                    color = Color.White, trackColor = Color.White.copy(alpha = 0.3f)
                )
                Text("$read / $total read in this chapter", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun ReflectCard(questions: List<String>) {
    AppCard(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth()) {
        Eyebrow("Reflect")
        Spacer(Modifier.height(8.dp))
        questions.forEachIndexed { i, q ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("${i + 1}. ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepGold)
                Text(q, fontSize = 14.sp, lineHeight = 21.sp, color = Ink)
            }
        }
    }
}

@Composable
fun ScholarCard(text: String, onFull: () -> Unit) {
    AppCard(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(), bg = GoldSoft) {
        Eyebrow("Scholar take · Sivananda")
        Spacer(Modifier.height(8.dp))
        Text(text, fontSize = 14.sp, lineHeight = 22.sp, color = Ink)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onFull, contentPadding = PaddingValues(0.dp)) { Text("Read full commentary", color = DeepGold, fontWeight = FontWeight.SemiBold) }
    }
}
