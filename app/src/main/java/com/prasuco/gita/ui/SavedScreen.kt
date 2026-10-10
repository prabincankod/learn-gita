package com.prasuco.gita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// ---------- saved verses ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(repo: Repository, openVerse: (Int, Int) -> Unit, goToday: () -> Unit) {
    val items by repo.bookmarks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    Surface(Modifier.fillMaxSize(), color = Bg) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { ScreenTitle("Saved") }
            items(items, key = { "${it.chapter}.${it.verse}" }) { b ->
                var gone by remember { mutableStateOf(false) }
                val dismissState = rememberSwipeToDismissBoxState()
                if (!gone) {
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFF4B4B)).padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) { Text("Remove", color = Color.White, fontWeight = FontWeight.Bold) }
                        },
                        content = {
                            OptionCard("Chapter ${b.chapter} · Verse ${b.verse}", null, false) { openVerse(b.chapter, b.verse) }
                        }
                    )
                }
                LaunchedEffect(dismissState.currentValue) {
                    if (!gone && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        gone = true
                        scope.launch { repo.db.users().removeBookmark(b.chapter, b.verse) }
                    }
                }
            }
            if (items.isEmpty()) item {
                EmptyState(
                    emoji = "🪷",
                    title = "Nothing saved yet",
                    sub = "Verses you save will wait for you here.",
                    action = "Read today's verse",
                    onAction = goToday
                )
            }
        }
    }
}
