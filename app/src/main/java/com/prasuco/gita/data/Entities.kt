package com.prasuco.gita.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey val number: Int,
    val nameSanskrit: String,
    val nameTransliterated: String,
    val nameTranslation: String,
    val meaning: String,
    val summary: String,
    val versesCount: Int
)

@Entity(tableName = "verses", primaryKeys = ["chapter", "verse"])
data class Verse(
    val chapter: Int,
    val verse: Int,
    val order: Int,
    val sanskrit: String,
    val transliteration: String,
    val wordMeanings: String
)

@Entity(tableName = "translations", primaryKeys = ["chapter", "verse", "source"])
data class Translation(
    val chapter: Int,
    val verse: Int,
    val source: String,
    val lang: String,
    val text: String
)

@Entity(tableName = "commentaries", primaryKeys = ["chapter", "verse", "source"])
data class Commentary(
    val chapter: Int,
    val verse: Int,
    val source: String,
    val lang: String,
    val text: String
)

@Entity(tableName = "bookmarks", primaryKeys = ["chapter", "verse"])
data class Bookmark(val chapter: Int, val verse: Int, val createdAt: Long = System.currentTimeMillis())

@Entity(tableName = "notes", primaryKeys = ["chapter", "verse"])
data class Note(val chapter: Int, val verse: Int, val text: String, val updatedAt: Long = System.currentTimeMillis())

@Entity(tableName = "progress", primaryKeys = ["chapter", "verse"])
data class VerseProgress(val chapter: Int, val verse: Int, val learnedAt: Long = System.currentTimeMillis())

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY number") fun all(): Flow<List<Chapter>>
    @Query("SELECT * FROM chapters ORDER BY number") suspend fun allOnce(): List<Chapter>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<Chapter>)
}

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE chapter = :ch ORDER BY verse") fun inChapter(ch: Int): Flow<List<Verse>>
    @Query("SELECT * FROM verses WHERE chapter = :ch ORDER BY verse") suspend fun inChapterOnce(ch: Int): List<Verse>
    @Query("SELECT * FROM verses WHERE chapter = :ch AND verse = :v LIMIT 1") suspend fun get(ch: Int, v: Int): Verse?
    @Query("SELECT * FROM verses ORDER BY `order`") suspend fun allOrdered(): List<Verse>
    // Offline FTS-lite: match sanskrit / transliteration. Translation search joins in repo.
    @Query("SELECT * FROM verses WHERE sanskrit LIKE '%' || :q || '%' OR transliteration LIKE '%' || :q || '%' LIMIT 50")
    suspend fun searchVerseText(q: String): List<Verse>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(items: List<Verse>)
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM translations WHERE chapter=:ch AND verse=:v ORDER BY source")
    suspend fun translations(ch: Int, v: Int): List<Translation>
    @Query("SELECT * FROM commentaries WHERE chapter=:ch AND verse=:v ORDER BY source")
    suspend fun commentaries(ch: Int, v: Int): List<Commentary>
    @Query("SELECT * FROM translations WHERE text LIKE '%' || :q || '%' LIMIT 50")
    suspend fun searchTranslations(q: String): List<Translation>
    @Query("SELECT * FROM commentaries WHERE text LIKE '%' || :q || '%' LIMIT 50")
    suspend fun searchCommentaries(q: String): List<Commentary>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTranslations(items: List<Translation>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCommentaries(items: List<Commentary>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC") fun bookmarks(): Flow<List<Bookmark>>
    @Query("SELECT * FROM bookmarks WHERE chapter=:ch AND verse=:v LIMIT 1") suspend fun bookmark(ch: Int, v: Int): Bookmark?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addBookmark(b: Bookmark)
    @Query("DELETE FROM bookmarks WHERE chapter=:ch AND verse=:v") suspend fun removeBookmark(ch: Int, v: Int)

    @Query("SELECT * FROM notes WHERE chapter=:ch AND verse=:v LIMIT 1") suspend fun note(ch: Int, v: Int): Note?
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC") fun notes(): Flow<List<Note>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveNote(n: Note)
    @Query("DELETE FROM notes WHERE chapter=:ch AND verse=:v") suspend fun deleteNote(ch: Int, v: Int)

    @Query("SELECT * FROM progress") fun progress(): Flow<List<VerseProgress>>
    @Query("SELECT * FROM progress") suspend fun progressOnce(): List<VerseProgress>
    @Query("SELECT COUNT(*) FROM progress") suspend fun learnedCount(): Int
    @Query("SELECT COUNT(*) FROM progress WHERE chapter=:ch") suspend fun learnedInChapter(ch: Int): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun markLearned(p: VerseProgress)
    @Query("DELETE FROM progress WHERE chapter=:ch AND verse=:v") suspend fun unmark(ch: Int, v: Int)
}
