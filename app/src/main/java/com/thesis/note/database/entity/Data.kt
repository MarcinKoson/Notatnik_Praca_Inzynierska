package com.thesis.note.database.entity

import androidx.room.*
import com.thesis.note.database.NoteType

@Entity(
    tableName = "data",
    foreignKeys = [
        ForeignKey( entity = Note::class,
                    parentColumns = arrayOf("IdNote"),
                    childColumns = arrayOf("NoteId"),
                    onDelete = ForeignKey.SET_NULL)
    ]
)
data class Data (
    @PrimaryKey(autoGenerate = true)
    var IdData: Int,
    var NoteId: Int,
    var Type: NoteType,
    var Content: String,
    var Info: String?
)
