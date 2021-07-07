package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Tag

@Dao
interface TagDAO {
    @Query("SELECT * FROM tag")
    fun getAll(): List<Tag>

    @Insert
    fun insertAll(vararg insert:Tag)

    @Delete
    fun delete(delete: Tag)

    @Update
    fun update(vararg update: Tag)
}