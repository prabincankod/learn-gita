package com.prasuco.gita.data

import java.time.LocalDate

/** Total verses in the bundle (18 chapters). Single source of truth — use everywhere. */
const val TOTAL_VERSES = 701

/** Source used for the daily two-line "Krishna saying". Short, plain English. */
const val SAYING_SOURCE = "Purohit_EN"

/** Max characters for the widget / notification two-liner. */
const val SAYING_MAX_CHARS = 140

/**
 * Today's shared verse + its two-line English saying.
 * Deterministic: everyone gets the same verse each day.
 */
suspend fun Repository.dailySaying(date: LocalDate = LocalDate.now()): Pair<Verse, String>? {
    val v = dailyVerse(date) ?: return null
    val trs = translations(v.chapter, v.verse)
    val raw = trs.firstOrNull { it.source == SAYING_SOURCE }?.text
        ?: trs.firstOrNull()?.text
        ?: return null
    return v to twoLiner(raw)
}

/**
 * The past [days] daily verses, newest first. Powers the "past week" carousel.
 */
suspend fun Repository.pastVerses(days: Int = 7, date: LocalDate = LocalDate.now()): List<Pair<LocalDate, Verse>> {
    val all = db.verses().allOrdered()
    if (all.isEmpty()) return emptyList()
    return (0 until days).map { back ->
        val d = date.minusDays(back.toLong())
        val idx = (d.dayOfYear + d.year * 37) % all.size
        d to all[idx]
    }
}

fun dayLabel(d: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    d == today -> "Today"
    d == today.minusDays(1) -> "Yesterday"
    else -> d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
}
/**
 * Shrinks a translation to a calm two-liner: strips verse-number prefixes,
 * collapses whitespace, cuts at a sentence boundary under [max], else at a
 * word boundary with an ellipsis. Pure — safe for widgets and tests.
 */
fun twoLiner(text: String, max: Int = SAYING_MAX_CHARS): String {
    var s = text.replace(Regex("^[।॥|\\.\\d\\s]+"), "").trim()
    s = s.replace(Regex("\\s+"), " ")
    // strip a leading "2.47 - " style reference if present
    s = s.replace(Regex("^\\d{1,2}[.:]\\d{1,3}\\s*[-–—.]\\s*"), "")
    if (s.length <= max) return s
    // prefer a sentence end (. ! ?) within the budget (min 40 chars kept)
    val cut = s.take(max)
    val sentenceEnd = listOf(". ", "! ", "? ", ".\"", "!\"", "?\"")
        .map { cut.lastIndexOf(it) }
        .filter { it >= 40 }
        .maxOrNull()
    if (sentenceEnd != null) return cut.take(sentenceEnd + 1).trim()
    // else word boundary
    val space = cut.lastIndexOf(' ')
    return (if (space >= 40) cut.take(space) else cut).trim() + "…"
}
