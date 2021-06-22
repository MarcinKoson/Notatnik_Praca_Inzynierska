package com.thesis.note.database.entity

import androidx.room.*

@Entity(
    tableName = "tagOfNote",
    foreignKeys = [
        ForeignKey( entity = Tag::class,
                    parentColumns = arrayOf("IdTag"),
                    childColumns = arrayOf("TagID"),
                    onDelete = ForeignKey.CASCADE),
        ForeignKey( entity = Note::class,
                    parentColumns = arrayOf("IdNote"),
                    childColumns = arrayOf("NoteID"),
                    onDelete = ForeignKey.CASCADE)
        ]
)
data class TagOfNote (
    @PrimaryKey(autoGenerate = true)
    var IdTagOfNote: Int,
    @ColumnInfo(index = true)
    var TagID: Int,
    @ColumnInfo(index = true)
    var NoteID: Int
)
