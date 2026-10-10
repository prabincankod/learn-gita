package com.prasuco.gita.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Chapter::class, Verse::class, Translation::class, Commentary::class, Bookmark::class, Note::class, VerseProgress::class],
    version = 1,
    exportSchema = false
)
abstract class GitaDatabase : RoomDatabase() {
    abstract fun chapters(): ChapterDao
    abstract fun verses(): VerseDao
    abstract fun content(): ContentDao
    abstract fun users(): UserDao

    companion object {
        @Volatile private var I: GitaDatabase? = null
        fun get(ctx: Context): GitaDatabase = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(ctx.applicationContext, GitaDatabase::class.java, "gita.db").build().also { I = it }
        }
    }
}
