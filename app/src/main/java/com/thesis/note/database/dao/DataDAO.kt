package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import java.util.*

@Dao
interface DataDAO {
    @Query("SELECT * FROM data")
    fun getAll(): List<Data>

    @Query("SELECT * FROM data WHERE NoteId = (:noteID)")
    fun getDataFromNote(noteID:Int):List<Data>



    @Insert
    fun insertAll(vararg insert: Data): List<Long>

    @Delete
    fun delete(delete: Data)

    @Update
    fun updateTodo(vararg update: Data)
}