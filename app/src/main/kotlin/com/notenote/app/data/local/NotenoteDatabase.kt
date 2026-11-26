package com.notenote.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notenote.app.data.local.converter.TagListConverter
import com.notenote.app.data.local.dao.FolderDao
import com.notenote.app.data.local.dao.NoteDao
import com.notenote.app.data.local.entity.FolderEntity
import com.notenote.app.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(TagListConverter::class)
abstract class NotenoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var instance: NotenoteDatabase? = null

        fun get(context: Context): NotenoteDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotenoteDatabase::class.java,
                    "notenote.db",
                ).build().also { instance = it }
            }
    }
}
