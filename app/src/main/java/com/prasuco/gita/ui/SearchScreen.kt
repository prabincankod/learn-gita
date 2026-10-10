package com.prasuco.gita.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.twoLiner
import kotlinx.coroutines.launch

// ---------- dilemma engine + search ----------

data class Dilemma(val name: String, val insight: String, val verses: List<Pair<Int, Int>>)

val DILEMMAS = listOf(
    Dilemma(
        "Anxiety",
        "Krishna's answer to anxiety is a shift of ownership: do the work in front of you with full care, and release the grip on how it turns out. Most dread lives in imagined futures, not in the present action. Act now; let the fruit arrive on its own time.",
        listOf(2 to 47, 2 to 48, 6 to 5, 18 to 66)
    ),
    Dilemma(
        "Grief",
        "Krishna does not ask the grieving to stop feeling — he widens the frame. What you love is not only the form that passed; the essence continues, and sorrow itself is a season, not a home. Sorrow is a season, not a home.",
        listOf(2 to 20, 2 to 14, 2 to 27, 18 to 66)
    ),
    Dilemma(
        "Indecision",
        "Arjuna was frozen exactly where you are — between two painful options. Krishna's method: get honest counsel, weigh your duty over your comfort, reflect deeply, and then choose with your whole heart. Clarity follows commitment, rarely the other way around.",
        listOf(2 to 7, 18 to 63, 3 to 35, 2 to 48)
    ),
    Dilemma(
        "Duty",
        "Your duty is not the most impressive task available — it is the one that is yours. Krishna ranks an imperfectly done own-duty above a perfectly done borrowed one. Stop auditioning for someone else's role; play your position fully.",
        listOf(2 to 47, 2 to 31, 3 to 35, 3 to 21)
    ),
    Dilemma(
        "Fear of Failure",
        "Failure stings most when your worth is riding on the outcome. Krishna separates the two: success and failure are weather, but steady effort is character. Stand up, act with a level mind, and let results be data, not verdicts.",
        listOf(2 to 47, 2 to 48, 2 to 3, 18 to 66)
    ),
    Dilemma(
        "Burnout",
        "Burnout is rarely about working hard — it is about working anxiously, pulled by craving and dread at once. Krishna prescribes evenness: same mind in effort and rest, in praise and blame. Recover your rhythm first; the output will follow.",
        listOf(2 to 48, 6 to 5, 3 to 35, 2 to 47)
    )
)

@Composable
fun SearchScreen(repo: Repository, openVerse: (Int, Int) -> Unit) {
    var q by remember { mutableStateOf("") }
    var res by remember { mutableStateOf(emptyList<Triple<Int, Int, String>>()) }
    var recents by remember { mutableStateOf(emptyList<String>()) }
    var picked by remember { mutableStateOf<Dilemma?>(null) }
    var dSayings by remember { mutableStateOf<Map<Pair<Int, Int>, String>>(emptyMap()) }
    var dLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by shimmer.animateFloat(0.45f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "shimmerA")
    LaunchedEffect(Unit) { recents = repo.prefs.recentSearches() }
    LaunchedEffect(picked) {
        dLoaded = false
        picked?.let { d ->
            dSayings = d.verses.associateWith { (ch, v) ->
                try {
                    val trs = repo.translations(ch, v)
                    trs.firstOrNull { it.source == "Purohit_EN" }?.text ?: trs.firstOrNull()?.text ?: ""
                } catch (_: Exception) { "" }
            }
            dLoaded = true
        }
    }
    fun submit(query: String) {
        scope.launch {
            res = repo.search(query)
            if (query.isNotBlank()) {
                repo.prefs.pushRecentSearch(query.trim())
                recents = repo.prefs.recentSearches()
            }
        }
    }
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp).padding(top = 20.dp)) {
                ScreenTitle("Search")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = q, onValueChange = { q = it; scope.launch { res = repo.search(q) } },
                    label = { Text("Search verses, meanings…") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit(q) }))
            }
            Spacer(Modifier.height(12.dp))
            if (q.isBlank()) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Eyebrow("What are you facing today?", Ink2) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(DILEMMAS) { d ->
                                AppPill(
                                    label = d.name,
                                    selected = picked == d,
                                    onClick = { picked = if (picked == d) null else d }
                                )
                            }
                        }
                    }
                    picked?.let { d ->
                        item {
                            AppCard(Modifier.fillMaxWidth()) {
                                Eyebrow("For ${d.name}")
                                Spacer(Modifier.height(8.dp))
                                Text(d.insight, fontSize = 15.sp, color = Ink, lineHeight = 23.sp)
                                Spacer(Modifier.height(12.dp))
                                Eyebrow("Core verses", Ink2)
                                Spacer(Modifier.height(8.dp))
                                if (!dLoaded) {
                                    repeat(4) {
                                        Box(
                                            Modifier.fillMaxWidth().height(68.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Pill.copy(alpha = shimmerAlpha))
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                } else {
                                    d.verses.forEach { (ch, v) ->
                                        OptionCard(
                                            "$ch.$v",
                                            dSayings[ch to v]?.takeIf { it.isNotBlank() } ?: "Tap to read",
                                            false,
                                            { openVerse(ch, v) },
                                            trailing = {
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(20.dp))
                                            }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (recents.isNotEmpty()) {
                        item {
                            Column {
                                Eyebrow("Recent", Ink2)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    recents.take(5).forEach { r ->
                                        AppPill(icon = Icons.Default.Search, label = r.take(18), onClick = { q = r; submit(r) })
                                    }
                                }
                            }
                        }
                    }
                    item { Eyebrow("Popular", Ink2) }
                    items(listOf(Triple(2, 47, "The classic on duty"), Triple(4, 7, "Divine descent"), Triple(18, 66, "Surrender"))) { (ch, v, s) ->
                        OptionCard(
                            "$ch.$v", s, false, { openVerse(ch, v) },
                            trailing = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(res) { (ch, v, src) ->
                        OptionCard(
                            "Chapter $ch · Verse $v", src, false, { openVerse(ch, v) },
                            trailing = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }
            }
        }
    }
}
