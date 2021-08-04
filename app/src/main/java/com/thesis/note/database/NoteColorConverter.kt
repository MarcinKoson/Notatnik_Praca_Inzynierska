package com.thesis.note.database

import android.content.res.Resources
import androidx.room.TypeConverter
import com.thesis.note.R

class NoteColorConverter{
    @TypeConverter
    fun intToEnum(value: Int?): NoteColor? {
        return if(value == null)
                null
            else
                when(value){
                    0 -> NoteColor.Black
                    1 -> NoteColor.White
                    2 -> NoteColor.Red
                    3 -> NoteColor.Pink
                    4 -> NoteColor.Purple
                    5 -> NoteColor.Blue
                    6 -> NoteColor.Cyan
                    7 -> NoteColor.Teal
                    8 -> NoteColor.Green
                    9 -> NoteColor.Yellow
                    10 -> NoteColor.Orange
                    11 -> NoteColor.Gray
                    else -> null
                }
    }
    @TypeConverter
    fun enumToInt(noteType: NoteColor?): Int? {
        return noteType?.id
    }
    fun intToColor(value: Int?,resources: Resources): Int{
        return when(value){
            0 -> resources.getColor(R.color.black,null)
            1 -> resources.getColor(R.color.white,null)
            2 -> resources.getColor(R.color.red_400,null)
            3 -> resources.getColor(R.color.pink_400,null)
            4 -> resources.getColor(R.color.purple_400,null)
            5 -> resources.getColor(R.color.blue_400,null)
            6 -> resources.getColor(R.color.cyan_400,null)
            7 -> resources.getColor(R.color.teal_400,null)
            8 -> resources.getColor(R.color.green_400,null)
            9 -> resources.getColor(R.color.yellow_400,null)
            10 -> resources.getColor(R.color.orange_400,null)
            11 -> resources.getColor(R.color.gray_400,null)
            else -> 0
        }
    }
}
