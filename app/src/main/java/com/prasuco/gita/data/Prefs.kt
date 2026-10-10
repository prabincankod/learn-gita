package com.prasuco.gita.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("gita_prefs")

class Prefs(private val ctx: Context) {
    private val ONBOARDED = booleanPreferencesKey("onboarded")
    private val GOAL = intPreferencesKey("daily_goal") // verses per day, or -100 = 1 chapter
    private val LEARN_MODE = stringPreferencesKey("learn_mode") // pace|daily|systematic
    private val REMINDER = booleanPreferencesKey("reminder_enabled")
    private val REMINDER_H = intPreferencesKey("reminder_hour")
    private val REMINDER_M = intPreferencesKey("reminder_min")
    private val TR_SOURCE = stringPreferencesKey("translation_source")
    private val CM_SOURCE = stringPreferencesKey("commentary_source")
    private val STREAK = intPreferencesKey("streak")
    private val STREAK_DAY = longPreferencesKey("streak_day")
    private val CURSOR_CH = intPreferencesKey("cursor_ch")
    private val CURSOR_V = intPreferencesKey("cursor_v")
    private val XP = intPreferencesKey("xp_total")
    private val SEEDED = booleanPreferencesKey("seeded_v1")

    suspend fun isOnboarded() = ctx.ds.data.map { it[ONBOARDED] == true }.first()
    suspend fun setOnboarded() = ctx.ds.edit { it[ONBOARDED] = true }
    suspend fun goal() = ctx.ds.data.map { it[GOAL] ?: 3 }.first()
    suspend fun setGoal(v: Int) = ctx.ds.edit { it[GOAL] = v }
    suspend fun learnMode() = ctx.ds.data.map { it[LEARN_MODE] ?: "daily" }.first()
    suspend fun setLearnMode(v: String) = ctx.ds.edit { it[LEARN_MODE] = v }
    suspend fun reminder() = ctx.ds.data.map { (it[REMINDER] != false) to Pair(it[REMINDER_H] ?: 7, it[REMINDER_M] ?: 0) }.first()
    suspend fun setReminder(enabled: Boolean, h: Int, m: Int) = ctx.ds.edit { it[REMINDER] = enabled; it[REMINDER_H] = h; it[REMINDER_M] = m }
    suspend fun trSource() = ctx.ds.data.map { it[TR_SOURCE] ?: "Purohit_EN" }.first()
    suspend fun setTrSource(v: String) = ctx.ds.edit { it[TR_SOURCE] = v }
    suspend fun cmSource() = ctx.ds.data.map { it[CM_SOURCE] ?: "Sivananda_EN" }.first()
    suspend fun setCmSource(v: String) = ctx.ds.edit { it[CM_SOURCE] = v }
    suspend fun cursor() = ctx.ds.data.map { (it[CURSOR_CH] ?: 1) to (it[CURSOR_V] ?: 1) }.first()
    suspend fun setCursor(ch: Int, v: Int) = ctx.ds.edit { it[CURSOR_CH] = ch; it[CURSOR_V] = v }
    suspend fun seeded() = ctx.ds.data.map { it[SEEDED] == true }.first()
    suspend fun setSeeded() = ctx.ds.edit { it[SEEDED] = true }
    suspend fun streak() = ctx.ds.data.map { (it[STREAK] ?: 0) to (it[STREAK_DAY] ?: 0L) }.first()
    suspend fun setStreak(n: Int, day: Long) = ctx.ds.edit { it[STREAK] = n; it[STREAK_DAY] = day }
    suspend fun xp() = ctx.ds.data.map { it[XP] ?: 0 }.first()
    suspend fun addXp(n: Int) = ctx.ds.edit { it[XP] = (it[XP] ?: 0) + n }
    private val RECENT_SEARCH = stringPreferencesKey("recent_searches")
    suspend fun recentSearches(): List<String> =
        ctx.ds.data.map { it[RECENT_SEARCH]?.split("|")?.filter { s -> s.isNotBlank() } ?: emptyList() }.first()
    suspend fun pushRecentSearch(q: String) = ctx.ds.edit {
        val cur = (it[RECENT_SEARCH]?.split("|") ?: emptyList()).filter { s -> s.isNotBlank() && s != q }
        it[RECENT_SEARCH] = (listOf(q) + cur).take(5).joinToString("|")
    }
}
