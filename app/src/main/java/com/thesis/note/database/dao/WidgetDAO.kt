package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Widget

@Dao
interface WidgetDAO {
    @Query("SELECT * FROM widget")
    fun getAll(): List<Widget>

    @Insert
    fun insertAll(vararg insert:Widget): List<Long>

    @Insert
    fun insert(insert: Data): Long

    @Delete
    fun delete(delete: Widget)

    @Update
    fun update(vararg update: Widget)
}
