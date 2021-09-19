package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Tag

@Dao
interface TagDAO {
    @Query("SELECT * FROM tag")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM 'tag' WHERE IdTag = (:tagId)")
    fun getId(tagId:Int): Tag

    @Insert
    fun insertAll(vararg insert:Tag): List<Long>

    @Insert
    fun insert(insert: Data): Long

    @Delete
    fun delete(delete: Tag)

    @Update
    fun update(vararg update: Tag)
}
