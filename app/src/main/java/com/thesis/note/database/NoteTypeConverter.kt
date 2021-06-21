package com.thesis.note.database

import androidx.room.TypeConverter

class NoteTypeConverter{

    @TypeConverter
    fun intToEnum(value: Int?): NoteType? {
        if(value == null )
            return null;
        else
            when(value){
                0 -> return NoteType.Text
                1 -> return NoteType.List
                2 -> return NoteType.Photo
                3 -> return NoteType.Sound
                4 -> return NoteType.Video
                else -> return null
            }
    }

    @TypeConverter
    fun enumToInt(notetype: NoteType?): Int? {
        return notetype?.id;
    }


}