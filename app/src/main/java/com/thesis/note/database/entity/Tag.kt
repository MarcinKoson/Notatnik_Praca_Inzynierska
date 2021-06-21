package com.thesis.note.database.entity

import androidx.room.*

@Entity(tableName = "tag")
data class Tag (
    @PrimaryKey(autoGenerate = true)
    var IdTag: Int,
    var Name: String

)
