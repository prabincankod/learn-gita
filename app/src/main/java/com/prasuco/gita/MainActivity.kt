package com.prasuco.gita

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.prasuco.gita.audio.VerseSpeaker
import com.prasuco.gita.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GitaApp
        val repo = app.repo
        setContent {
            GitaTheme {
                val nav = rememberNavController()
                var start by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    // deep link from widgets: gita://verse/2/47 etc.
                    handleIntent(intent)?.let { start = it }
                    if (start == null) start = if (repo.prefs.isOnboarded()) Routes.HOME else Routes.ONBOARDING
                }
                // splash gates on this; shows instantly, navigates when ready
                // deep-linked screens fall back Home when the stack is empty
                val backOrHome: () -> Unit = { if (!nav.popBackStack()) nav.navigate(Routes.HOME) }
                val back by nav.currentBackStackEntryAsState()
                val route = back?.destination?.route ?: ""
                // calm reader: bottom bar everywhere except onboarding + verse reader + splash
                val showBar = route != Routes.ONBOARDING && !route.startsWith("verse") && route != Routes.SPLASH
                Scaffold(
                    containerColor = Bg,
                    bottomBar = {
                        Column {
                            if (VerseSpeaker.speaking) {
                                Row(
                                    Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                                        .modernCard(RoundedCornerShape(28.dp))
                                        .clickable { VerseSpeaker.stop() }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.VolumeUp, null, tint = DeepGold, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Playing · ${VerseSpeaker.label.ifBlank { "today's verse" }}",
                                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("Stop", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepGold)
                                }
                            }
                            if (showBar) NavigationBar(containerColor = Color.White) {
                            NavigationBarItem(selected = route == Routes.HOME, onClick = { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) } }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Today") })
                            NavigationBarItem(selected = route.startsWith("chapter"), onClick = { nav.navigate(Routes.CHAPTERS) }, icon = { Icon(Icons.Default.Book, null) }, label = { Text("Chapters") })
                            NavigationBarItem(selected = route == Routes.SEARCH, onClick = { nav.navigate(Routes.SEARCH) }, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search") })
                            NavigationBarItem(selected = route == Routes.BOOKMARKS, onClick = { nav.navigate(Routes.BOOKMARKS) }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
                        }
                        }
                    }
                ) { pad ->
                    NavHost(nav, startDestination = Routes.SPLASH, modifier = Modifier.padding(pad)) {
                        composable(Routes.SPLASH) {
                            SplashScreen(repo, start) { dest -> nav.navigate(dest) { popUpTo(0) } }
                        }
                        composable(Routes.ONBOARDING) { OnboardingScreen(repo) { nav.navigate(Routes.HOME) { popUpTo(0) } } }
                        composable(Routes.HOME) {
                            HomeScreen(repo,
                                goVerse = { ch, v -> nav.navigate(Routes.verse(ch, v)) })
                        }
                        composable(Routes.CHAPTERS) { ChaptersScreen(repo) { nav.navigate(Routes.chapter(it)) } }
                        composable(Routes.CHAPTER, arguments = listOf(navArgument("ch") { type = NavType.IntType })) {
                            val ch = it.arguments?.getInt("ch") ?: 1
                            ChapterDetailScreen(repo, ch, { c, v -> nav.navigate(Routes.verse(c, v)) }, backOrHome)
                        }
                        composable(
                            Routes.VERSE,
                            arguments = listOf(navArgument("ch") { type = NavType.IntType }, navArgument("v") { type = NavType.IntType }),
                            deepLinks = listOf(navDeepLink { uriPattern = "gita://verse/{ch}/{v}" })
                        ) {
                            val ch = it.arguments?.getInt("ch") ?: 1
                            val v = it.arguments?.getInt("v") ?: 1
                            VerseScreen(repo, ch, v, backOrHome)
                        }
                        composable(Routes.SEARCH) { SearchScreen(repo) { ch, v -> nav.navigate(Routes.verse(ch, v)) } }
                        composable(Routes.BOOKMARKS) { SavedScreen(repo, { ch, v -> nav.navigate(Routes.verse(ch, v)) }, { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) } }) }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(i: Intent): String? {
        val d = i.data ?: return null
        return when (d.host) {
            "verse" -> {
                val seg = d.pathSegments
                if (seg.size >= 2) Routes.verse(seg[0].toIntOrNull() ?: 1, seg[1].toIntOrNull() ?: 1) else null
            }
            "learn" -> Routes.HOME
            "progress" -> Routes.CHAPTERS
            else -> null
        }
    }
}
