package com.thesis.note.database

import androidx.room.TypeConverter

class NoteColorConverter{

    @TypeConverter
    fun intToEnum(value: Int?): NoteColor? {
        return if(value == null)
                null
            else
                when(value){
                    0 -> NoteColor.Black
                    1 -> NoteColor.White
                    else -> null
                }
    }
    @TypeConverter
    fun enumToInt(noteType: NoteColor?): Int? {
        return noteType?.id
    }
}
