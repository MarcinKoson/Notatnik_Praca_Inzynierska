package com.thesis.note.database

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    @TypeConverter
    fun stringToDate(value: String?): Date? {
        return if(value != null)
            SimpleDateFormat("yyyy.MM.dd-HH:mm", Locale.US).parse(value)
        else null
    }
    @TypeConverter
    fun dateToString(value: Date?): String? {
        return if(value != null)
            SimpleDateFormat("yyyy.MM.dd-HH:mm", Locale.US).format(value)
        else null
    }
}