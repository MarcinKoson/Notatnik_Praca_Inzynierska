package com.thesis.note.database.entity

import androidx.room.*
import com.thesis.note.database.Color

@Entity(tableName = "textWidget")
data class TextWidget (
    @PrimaryKey(autoGenerate = true)
    var IdTextWidget: Int,
    var WidgetId: Int,
    var Content: String,
    var Size: Float,
    var Color: Color,
    var FontColor: Color
)
