package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.TextWidget

@Dao
interface TextWidgetDAO {
    @Query("SELECT * FROM textWidget")
    fun getAll(): List<TextWidget>

    @Query("SELECT * FROM textWidget WHERE IdTextWidget = (:textWidgetId)")
    fun getTextWidgetById(textWidgetId:Int): TextWidget

    @Insert
    fun insertAll(vararg insert:TextWidget): List<Long>

    @Insert
    fun insert(insert: TextWidget): Long

    @Delete
    fun delete(delete: TextWidget)

    @Update
    fun update(vararg update: TextWidget)
}
