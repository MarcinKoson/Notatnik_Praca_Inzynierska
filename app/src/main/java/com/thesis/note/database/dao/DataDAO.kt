package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Data

@Dao
interface DataDAO {
    @Query("SELECT * FROM data")
    fun getAll(): List<Data>

    @Query("SELECT * FROM data WHERE NoteId = (:noteID)")
    fun getDataFromNote(noteID:Int):List<Data>

    @Query("SELECT * FROM data WHERE IdData = (:noteID)")
    fun getDataById(noteID:Int): Data

    @Insert
    fun insertAll(vararg insert: Data): List<Long>

    @Insert
    fun insert(insert: Data): Long

    @Delete
    fun delete(delete: Data)

    @Update
    fun update(vararg update: Data)
}
