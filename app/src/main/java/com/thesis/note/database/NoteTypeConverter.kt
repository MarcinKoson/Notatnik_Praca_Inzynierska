package com.thesis.note.database

import androidx.room.TypeConverter

class NoteTypeConverter{

    @TypeConverter
    fun intToEnum(value: Int?): NoteType? {
        return if(value == null)
                null
            else
                when(value){
                    0 -> NoteType.Text
                    1 -> NoteType.List
                    2 -> NoteType.Photo
                    3 -> NoteType.Sound
                    4 -> NoteType.Video
                    else -> null
                }
    }
    @TypeConverter
    fun enumToInt(noteType: NoteType?): Int? {
        return noteType?.id
    }
}
