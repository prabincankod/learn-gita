package com.prasuco.gita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prasuco.gita.data.Repository
import com.prasuco.gita.data.Verse
import kotlinx.coroutines.launch

// ---------- verse reader ----------

@Composable
fun VerseScreen(repo: Repository, ch: Int, v: Int) {
    val scope = rememberCoroutineScope()
    var showSave by remember { mutableStateOf(false) }
    var verse by remember { mutableStateOf<Verse?>(null) }
    var trs by remember { mutableStateOf(emptyList<com.prasuco.gita.data.Translation>()) }
    var cms by remember { mutableStateOf(emptyList<com.prasuco.gita.data.Commentary>()) }
    var trSel by remember { mutableStateOf("") }
    var cmSel by remember { mutableStateOf("") }
    var marked by remember { mutableStateOf(false) }
    var bookmarked by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    LaunchedEffect(ch, v) {
        verse = repo.verse(ch, v)
        trs = repo.translations(ch, v); cms = repo.commentaries(ch, v)
        trSel = repo.prefs.trSource(); cmSel = repo.prefs.cmSource()
        marked = repo.db.users().progressOnce().any { it.chapter == ch && it.verse == v }
        bookmarked = repo.db.users().bookmark(ch, v) != null
        note = repo.db.users().note(ch, v)?.text ?: ""
    }
    val tr = trs.firstOrNull { it.source == trSel } ?: trs.firstOrNull()
    val cm = cms.firstOrNull { it.source == cmSel } ?: cms.firstOrNull()
    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize().weight(1f).padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 20.dp, bottom = 12.dp)) {
            item {
                Eyebrow("Verse $ch.$v")
                Spacer(Modifier.height(8.dp))
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Eyebrow("Translation", Ink2)
                        if (trs.size > 1) {
                            var trMenu by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { trMenu = true }, contentPadding = PaddingValues(0.dp)) {
                                    Text("by ${friendlySource(tr?.source ?: "")}", fontSize = 13.sp, color = Ink2)
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Ink2, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(expanded = trMenu, onDismissRequest = { trMenu = false }) {
                                    trs.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(friendlySource(t.source)) },
                                            onClick = { scope.launch { repo.prefs.setTrSource(t.source); trSel = t.source; trMenu = false } }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tr?.text ?: "…", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Ink, lineHeight = 29.sp)
                }
                Spacer(Modifier.height(12.dp))
                cm?.text?.let {
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Eyebrow("Scholar take", Ink2)
                            if (cms.size > 1) {
                                var cmMenu by remember { mutableStateOf(false) }
                                Box {
                                    TextButton(onClick = { cmMenu = true }, contentPadding = PaddingValues(0.dp)) {
                                        Text("by ${friendlySource(cm?.source ?: "")}", fontSize = 13.sp, color = Ink2)
                                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Ink2, modifier = Modifier.size(16.dp))
                                    }
                                    DropdownMenu(expanded = cmMenu, onDismissRequest = { cmMenu = false }) {
                                        cms.forEach { t ->
                                            DropdownMenuItem(
                                                text = { Text(friendlySource(t.source)) },
                                                onClick = { scope.launch { repo.prefs.setCmSource(t.source); cmSel = t.source; cmMenu = false } }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(it, fontSize = 14.sp, color = Ink2, lineHeight = 22.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it }, label = { Text("My note") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Pill, unfocusedContainerColor = Pill,
                        focusedBorderColor = Gold, unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(8.dp))
                DuoButton("Save note", { scope.launch { repo.db.users().saveNote(com.prasuco.gita.data.Note(ch, v, note)) } })
            }
            }
            // sticky bottom console: mark read - save - share
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1A0F172A), spotColor = Color(0x1A0F172A))
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { verse?.let { repo.markLearned(it); marked = true } } }) {
                    Icon(
                        if (marked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        "mark read", tint = if (marked) Success else Ink2, modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = { scope.launch {
                    if (bookmarked) { repo.db.users().removeBookmark(ch, v); bookmarked = false }
                    else { repo.db.users().addBookmark(com.prasuco.gita.data.Bookmark(ch, v)); bookmarked = true }
                } }) {
                    Icon(
                        if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        "save", tint = if (bookmarked) DeepGold else Ink2, modifier = Modifier.size(26.dp))
                }
                IconButton(onClick = { showSave = true }) {
                    Icon(Icons.Default.Download, "save image", tint = Ink2, modifier = Modifier.size(26.dp))
                }
            }
            if (showSave) SaveImageSheet(ch, v, tr?.text ?: "") { showSave = false }
        }
    }
}
