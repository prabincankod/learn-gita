package com.prasuco.gita.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.prasuco.gita.data.TOTAL_VERSES
import com.prasuco.gita.share.ShareCards
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ---------- canonical atoms: use these, never rebuild ----------

@Composable
fun Eyebrow(text: String, color: Color = DeepGold, modifier: Modifier = Modifier) {
    Text(text.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color, letterSpacing = 1.sp, modifier = modifier)
}

@Composable
fun ScreenTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Ink)
}

@Composable
fun AppCard(
    outer: Modifier = Modifier,
    bg: Color = Color.White,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(outer.modernCard(shape, bg).padding(20.dp), content = content)
}

@Composable
fun ProgressBar(value: Float, small: Boolean = false) {
    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(if (small) 6.dp else 10.dp)
            .clip(RoundedCornerShape(if (small) 4.dp else 6.dp)),
        color = Gold, trackColor = Divider
    )
}

/** The one pill. Icon optional, selected = amber tint. */
@Composable
fun AppPill(
    label: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        Modifier.clip(RoundedCornerShape(24.dp))
            .background(if (selected) GoldSoft else Pill)
            .border(if (selected) BorderStroke(1.dp, Gold) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(24.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (selected) DeepGold else Ink2, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (selected) AmberInk else Ink)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, sub: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = DeepGold, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Ink)
        Text(sub, color = Ink2, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        DuoButton(action, onAction)
    }
}

/** Bottom sheet: pick Square/Story, render on the fly, save to gallery. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveImageSheet(ch: Int, v: Int, text: String, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val ref = "$ch.$v"

    fun save(format: ShareCards.Format) {
        if (busy || text.isBlank()) return
        busy = true
        scope.launch(Dispatchers.IO) {
            val bmp = ShareCards.render(ctx, ch, ref, text, format, ShareCards.Preset.SUNSET, null, ShareCards.Decor.NONE, true, true)
            val ok = ShareCards.saveToGallery(ctx, bmp, ref, format)
            launch(Dispatchers.Main) {
                busy = false
                Toast.makeText(ctx, if (ok) "Saved to gallery" else "Could not save", Toast.LENGTH_SHORT).show()
                if (ok) onDone()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDone, containerColor = Color.White) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Eyebrow("Save verse image")
            Spacer(Modifier.height(8.dp))
            Text("Square for chats, story for Status.", fontSize = 14.sp, color = Ink2)
            Spacer(Modifier.height(12.dp))
            AppPill("Square 1:1", { save(ShareCards.Format.SQUARE) }, enabled = !busy)
            Spacer(Modifier.height(8.dp))
            AppPill("Story 9:16", { save(ShareCards.Format.STORY) }, enabled = !busy)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------- shared buttons & cards ----------

@Composable
fun DuoButton(text: String, onClick: () -> Unit, enabled: Boolean = true, secondary: Boolean = false, modifier: Modifier = Modifier) {
    val label = text.ifBlank { "Continue" }
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(28.dp),
        colors = if (secondary)
            ButtonDefaults.buttonColors(containerColor = Pill, contentColor = Ink, disabledContainerColor = Pill)
        else
            ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White, disabledContainerColor = Color(0xFFD7D7D7))
    ) {
        Text(label.uppercase(), fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp,
            color = if (secondary) Ink else Color.White, maxLines = 1)
    }
}

@Composable
fun TopBar(streak: Int, read: Int, onGear: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.clip(RoundedCornerShape(24.dp)).background(Pill).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Whatshot, null, tint = Gold, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("$streak day${if (streak == 1) "" else "s"}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
            Spacer(Modifier.width(12.dp))
            Text("$read / $TOTAL_VERSES", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink2)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onGear) { Icon(Icons.Default.Settings, "settings", tint = Ink2) }
    }
}

@Composable
fun OptionCard(
    label: String,
    sub: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().modernCard(shape)
            .border(if (selected) BorderStroke(2.dp, Gold) else BorderStroke(0.dp, Color.Transparent), shape)
            .background(if (selected) GoldSoft else Color.White, shape)
            .clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
            if (sub != null) Text(sub, fontSize = 13.sp, color = Ink2, modifier = Modifier.padding(top = 2.dp))
        }
        if (selected) Icon(Icons.Default.Check, null, tint = DeepGold)
        trailing?.invoke()
    }
}

fun friendlySource(src: String): String = when (src) {
    "Purohit_EN" -> "Swami Purohit"
    "Sivananda_EN" -> "Swami Sivananda"
    "Ramsukhdas_HI" -> "Swami Ramsukhdas"
    "Tejomayananda_HI" -> "Swami Tejomayananda"
    else -> src.replace("_", " ")
}

fun reflectionFor(order: Int): List<String> {
    val bank = listOf(
        "What duty in your life right now feels like Arjuna's — clear, yet hard to face?",
        "Which line of this verse argues with how you acted yesterday?",
        "If you lived this verse for one day, what would you do differently before noon?",
        "What attachment does this verse quietly ask you to loosen?",
        "Who in your life needs the steadiness this verse describes?",
        "What fear shrinks when you read this verse slowly, twice?"
    )
    return (0 until 3).map { bank[(order + it * 2) % bank.size] } +
        "Which single word from this verse will you carry through today?"
}

fun fmtTime(h: Int, m: Int): String {
    val suffix = if (h < 12) "AM" else "PM"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$h12:${m.toString().padStart(2, '0')} $suffix"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeDialog(h: Int, m: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK", color = Ink) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Ink2) } },
        containerColor = Color.White,
        text = { TimePicker(state = state) }
    )
}

@Composable
fun TopBarBack(title: String, subtitle: String? = null, onBack: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = Ink2)
            }
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "back", tint = Ink) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
    )
}
