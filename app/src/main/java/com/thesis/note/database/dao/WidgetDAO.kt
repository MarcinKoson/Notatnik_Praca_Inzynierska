package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Widget

@Dao
interface WidgetDAO {
    @Query("SELECT * FROM widget")
    fun getAll(): List<Widget>

    @Insert
    fun insertAll(vararg insert:Widget)

    @Delete
    fun delete(delete: Widget)

    @Update
    fun update(vararg update: Widget)
}