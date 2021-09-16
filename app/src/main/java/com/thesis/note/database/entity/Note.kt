package com.thesis.note.database.entity

import androidx.room.*
import com.thesis.note.database.NoteColor
import java.util.*

@Entity(
    tableName = "note",
    foreignKeys = [
        ForeignKey( entity = Group::class,
                    parentColumns = arrayOf("IdGroup"),
                    childColumns = arrayOf("GroupID"),
                    onDelete = ForeignKey.SET_NULL),
        ForeignKey( entity = Data::class,
                    parentColumns = arrayOf("IdData"),
                    childColumns = arrayOf("MainData"),
                    onDelete = ForeignKey.SET_NULL)
    ]
)
data class Note (
    @PrimaryKey(autoGenerate = true)
    var IdNote: Int,
    var Name: String,
    var MainData: Int?,
    var GroupID: Int?,
    var Favorite: Boolean,
    var Importance: Int?,
    var Date: Date?,
    var Location: String?,
    var Color: NoteColor?
)
