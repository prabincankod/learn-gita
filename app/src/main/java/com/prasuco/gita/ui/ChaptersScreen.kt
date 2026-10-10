package com.prasuco.gita.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Chapter
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.TOTAL_VERSES

// ---------- chapters library + detail ----------

@Composable
fun ChaptersScreen(repo: Repository, open: (Int) -> Unit) {
    val chapters by repo.chapters().collectAsState(initial = emptyList())
    var counts by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var total by remember { mutableIntStateOf(0) }
    LaunchedEffect(chapters) {
        counts = chapters.associate { it.number to repo.learnedInChapter(it.number) }
        total = try { repo.learnedCount() } catch (_: Exception) { 0 }
    }
    Surface(Modifier.fillMaxSize(), color = Bg) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenTitle("Chapters") }
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Eyebrow("My journey")
                    Spacer(Modifier.height(4.dp))
                    Text("$total / $TOTAL_VERSES verses read", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(total / TOTAL_VERSES.toFloat())
                }
            }
            items(chapters) { c: Chapter ->
                val done = counts[c.number] ?: 0
                AppCard(Modifier.fillMaxWidth().clickable { open(c.number) }, shape = RoundedCornerShape(16.dp)) {
                    Eyebrow("Chapter ${c.number}")
                    Text(c.nameTranslation.ifBlank { c.nameTransliterated }, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink)
                    Text("${c.versesCount} verses · $done read", fontSize = 13.sp, color = Ink2)
                    Spacer(Modifier.height(8.dp))
                    ProgressBar(if (c.versesCount == 0) 0f else done / c.versesCount.toFloat())
                }
            }
        }
    }
}
