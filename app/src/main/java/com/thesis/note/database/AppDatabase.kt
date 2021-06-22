package com.thesis.note.database

import android.content.Context
import androidx.room.*
import com.thesis.note.database.dao.*
import com.thesis.note.database.entity.*

@Database(entities = [Note::class, Group::class, Tag::class, TagOfNote::class,Widget::class,Data::class], version = 1,exportSchema = false)
@TypeConverters(NoteTypeConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun noteDao(): NoteDAO
    abstract fun tagDao(): TagDAO
    abstract fun groupDao(): GroupDAO
    abstract fun tagOfNoteDAO(): TagOfNoteDAO
    abstract fun widgetDao(): WidgetDAO
    abstract fun dataDao(): DataDAO

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance
            ?: synchronized(LOCK){
            instance
                ?: buildDatabase(context).also { instance = it}
        }
        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDatabase::class.java, "testDB.db").build()
    }
}
