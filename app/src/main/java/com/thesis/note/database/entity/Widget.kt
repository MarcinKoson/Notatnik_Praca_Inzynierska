package com.thesis.note.database.entity

import androidx.room.*

@Entity(tableName = "widget")
data class Widget (
    @PrimaryKey(autoGenerate = true)
    var IdWidget: Int,
    var NoteId: Int,
    var WidgetID: Int
)
