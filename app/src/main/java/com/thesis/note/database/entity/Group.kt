package com.thesis.note.database.entity

import androidx.room.*

@Entity(tableName = "group")
data class Group (
    @PrimaryKey(autoGenerate = true)
    var IdGroup: Int,
    var Name: String,
    var Color: Int?
)
