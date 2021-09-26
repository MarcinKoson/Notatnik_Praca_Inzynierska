package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.TagOfNote

@Dao
interface TagOfNoteDAO {
    @Query("SELECT * FROM tagOfNote")
    fun getAll(): List<TagOfNote>

    @Query("SELECT * FROM tagOfNote WHERE NoteID = (:noteID)")
    fun getAllNoteTags(noteID:Int): List<TagOfNote>

    @Insert
    fun insertAll(vararg insert: TagOfNote): List<Long>

    @Insert
    fun insert(insert: TagOfNote): Long

    @Delete
    fun delete(delete: TagOfNote)

    @Update
    fun update(vararg update: TagOfNote)
}
