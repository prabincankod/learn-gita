package com.prasuco.gita.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

class Repository(val db: GitaDatabase, val prefs: Prefs) {
    fun chapters(): Flow<List<Chapter>> = db.chapters().all()
    fun versesIn(ch: Int): Flow<List<Verse>> = db.verses().inChapter(ch)
    fun bookmarks() = db.users().bookmarks()
    fun notes() = db.users().notes()
    fun progress() = db.users().progress()
    suspend fun verse(ch: Int, v: Int) = db.verses().get(ch, v)
    suspend fun translations(ch: Int, v: Int) = db.content().translations(ch, v)
    suspend fun commentaries(ch: Int, v: Int) = db.content().commentaries(ch, v)
    suspend fun learnedCount() = db.users().learnedCount()
    suspend fun learnedInChapter(ch: Int) = db.users().learnedInChapter(ch)

    suspend fun search(q: String): List<Triple<Int, Int, String>> {
        if (q.isBlank()) return emptyList()
        val out = LinkedHashMap<Pair<Int, Int>, String>()
        db.verses().searchVerseText(q).forEach { out[it.chapter to it.verse] = "Sanskrit" }
        db.content().searchTranslations(q).forEach { out[it.chapter to it.verse] = it.source }
        db.content().searchCommentaries(q).forEach { out.getOrPut(it.chapter to it.verse) { it.source } }
        // chapter/verse number query like "2.47"
        Regex("""(\d{1,2})[.\s:](\d{1,3})""").find(q.trim())?.let {
            val ch = it.groupValues[1].toIntOrNull(); val v = it.groupValues[2].toIntOrNull()
            if (ch != null && v != null && db.verses().get(ch, v) != null) out[ch to v] = "Reference"
        }
        return out.entries.take(50).map { Triple(it.key.first, it.key.second, it.value) }
    }

    /** Deterministic daily verse: day-of-year mapped over all 700 verses. */
    suspend fun dailyVerse(date: LocalDate = LocalDate.now()): Verse? {
        val all = db.verses().allOrdered()
        if (all.isEmpty()) return null
        val idx = (date.dayOfYear + date.year * 37) % all.size
        return all[idx]
    }

    /** Today's lesson: next N unseen verses from cursor. */
    suspend fun todayLesson(date: LocalDate = LocalDate.now()): List<Verse> {
        val goal = prefs.goal()
        val all = db.verses().allOrdered()
        if (all.isEmpty()) return emptyList()
        val done = db.users().progressOnce().map { it.chapter to it.verse }.toSet()
        val (cCh, cV) = prefs.cursor()
        var start = all.indexOfFirst { it.chapter == cCh && it.verse == cV }.takeIf { it >= 0 } ?: 0
        // advance cursor past already-learned
        while (start < all.size && (all[start].chapter to all[start].verse) in done) start++
        if (start >= all.size) return emptyList()
        if (goal <= -100) {
            val ch = all[start].chapter
            return all.filter { it.chapter == ch && (it.chapter to it.verse) !in done }
        }
        val n = goal.coerceIn(1, 20)
        return all.drop(start).filter { (it.chapter to it.verse) !in done }.take(n)
    }

    suspend fun markLearned(v: Verse) {
        db.users().markLearned(VerseProgress(v.chapter, v.verse))
        // advance cursor
        val all = db.verses().allOrdered()
        val i = all.indexOfFirst { it.chapter == v.chapter && it.verse == v.verse }
        if (i >= 0 && i + 1 < all.size) prefs.setCursor(all[i + 1].chapter, all[i + 1].verse)
        touchStreak()
    }

    private suspend fun touchStreak() {
        val today = LocalDate.now().toEpochDay()
        val (n, day) = prefs.streak()
        when {
            day == today -> Unit
            day == today - 1 -> prefs.setStreak(n + 1, today)
            else -> prefs.setStreak(1, today)
        }
    }

    suspend fun ensureSeeded(ctx: Context) {
        if (prefs.seeded()) return
        if (db.chapters().allOnce().isNotEmpty()) { prefs.setSeeded(); return }
        withContext(Dispatchers.IO) {
            val raw = ctx.assets.open("gita_bundle.json").bufferedReader().readText()
            val root = JSONObject(raw)
            val chs = root.getJSONArray("chapters")
            val chapters = (0 until chs.length()).map { i ->
                val o = chs.getJSONObject(i)
                Chapter(o.getInt("number"), o.getString("nameSanskrit"), o.getString("nameTransliterated"), o.getString("nameTranslation"), o.optString("meaning", ""), o.optString("summary", ""), o.getInt("versesCount"))
            }
            db.chapters().upsertAll(chapters)
            val vs = root.getJSONArray("verses")
            val verses = (0 until vs.length()).map { i ->
                val o = vs.getJSONObject(i)
                Verse(o.getInt("chapter"), o.getInt("verse"), o.optInt("order", 0), o.getString("sanskrit"), o.optString("transliteration", ""), o.optString("wordMeanings", ""))
            }
            // chunk inserts to avoid binder limits
            verses.chunked(200).forEach { db.verses().upsertAll(it) }
            val trs = root.getJSONArray("translations")
            (0 until trs.length()).map { i ->
                val o = trs.getJSONObject(i)
                Translation(o.getInt("chapter"), o.getInt("verse"), o.getString("source"), o.getString("lang"), o.getString("text"))
            }.chunked(400).forEach { db.content().upsertTranslations(it) }
            val cms = root.getJSONArray("commentaries")
            (0 until cms.length()).map { i ->
                val o = cms.getJSONObject(i)
                Commentary(o.getInt("chapter"), o.getInt("verse"), o.getString("source"), o.getString("lang"), o.getString("text"))
            }.chunked(400).forEach { db.content().upsertCommentaries(it) }
        }
        prefs.setSeeded()
    }
}
