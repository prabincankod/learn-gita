package com.prasuco.gita.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.prasuco.gita.data.Repository

@Composable
fun ChapterDetailScreen(repo: Repository, ch: Int, openVerse: (Int, Int) -> Unit, onBack: () -> Unit) {
    val chapters by repo.chapters().collectAsState(initial = emptyList())
    val verses by repo.versesIn(ch).collectAsState(initial = emptyList())
    val bookmarks by repo.bookmarks().collectAsState(initial = emptyList())
    var learnedSet by remember { mutableStateOf<Set<Int>>(emptySet()) }
    LaunchedEffect(ch) {
        learnedSet = repo.db.users().progressOnce().filter { it.chapter == ch }.map { it.verse }.toSet()
    }
    val savedSet = bookmarks.filter { it.chapter == ch }.map { it.verse }.toSet()
    val meta = chapters.firstOrNull { it.number == ch }
    val theme = remember(meta) {
        meta?.summary?.split(Regex("(?<=\\.)\\s+"))?.take(2)?.joinToString(" ")?.take(220) ?: ""
    }
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            TopBarBack(
                "Chapter $ch",
                meta?.nameTranslation?.ifBlank { meta.nameTransliterated },
                onBack
            )
            AppCard(Modifier.padding(20.dp), shape = RoundedCornerShape(24.dp)) {
                if (meta?.meaning?.isNotBlank() == true) Text(meta.meaning, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink)
                if (theme.isNotBlank()) Text(theme, fontSize = 13.sp, color = Ink2, modifier = Modifier.padding(top = 6.dp), lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                val read = learnedSet.size
                val totalN = verses.size.takeIf { it > 0 } ?: (meta?.versesCount ?: 0)
                Row(Modifier.clip(RoundedCornerShape(24.dp)).background(Pill).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$read / $totalN read", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Spacer(Modifier.height(8.dp))
                ProgressBar(if (totalN == 0) 0f else read / totalN.toFloat(), small = true)
            }
            val nextUp = verses.firstOrNull { it.verse !in learnedSet }?.verse
            LazyVerticalGrid(GridCells.Fixed(4), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(verses) { v ->
                    val done = v.verse in learnedSet
                    val saved = v.verse in savedSet
                    val isNext = !done && v.verse == nextUp
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        Modifier.aspectRatio(1f).modernCard(shape)
                            .background(when { isNext -> GoldSoft; done -> Color.White; else -> Pill }, shape)
                            .border(if ((saved && !isNext) || isNext) BorderStroke(2.dp, Gold) else BorderStroke(0.dp, Color.Transparent), shape)
                            .clickable { openVerse(ch, v.verse) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${v.verse}", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                                color = when { isNext -> AmberInk; done -> Ink; else -> Ink2 })
                            if (done || isNext) Icon(Icons.Default.Check, null, tint = Gold, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
