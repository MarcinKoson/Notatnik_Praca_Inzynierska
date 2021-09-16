package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Group

@Dao
interface GroupDAO {
    @Query("SELECT * FROM 'group'")
    fun getAll(): List<Group>

    @Query("SELECT * FROM 'group' WHERE IdGroup = (:groupId)")
    fun getId(groupId:Int): Group

    @Insert
    fun insertAll(vararg insert: Group): List<Long>

    @Insert
    fun insert(insert: Data): Long

    @Delete
    fun delete(delete: Group)

    @Update
    fun update(vararg update: Group)
}