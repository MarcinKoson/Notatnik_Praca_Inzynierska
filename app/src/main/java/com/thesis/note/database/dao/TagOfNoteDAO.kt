package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.TagOfNote

@Dao
interface TagOfNoteDAO {
    @Query("SELECT * FROM tagofnote")
    fun getAll(): List<TagOfNote>

    @Insert
    fun insertAll(vararg insert: TagOfNote)

    @Delete
    fun delete(delete: TagOfNote)

    @Update
    fun updateTodo(vararg update: TagOfNote)
}