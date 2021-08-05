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
    fun enumToColor(value: NoteColor?,resources: Resources): Int{
        return when(value){
            NoteColor.Black -> resources.getColor(R.color.black,null)
            NoteColor.White -> resources.getColor(R.color.white,null)
            NoteColor.Red -> resources.getColor(R.color.red_400,null)
            NoteColor.Pink -> resources.getColor(R.color.pink_400,null)
            NoteColor.Purple -> resources.getColor(R.color.purple_400,null)
            NoteColor.Blue -> resources.getColor(R.color.blue_400,null)
            NoteColor.Cyan -> resources.getColor(R.color.cyan_400,null)
            NoteColor.Teal -> resources.getColor(R.color.teal_400,null)
            NoteColor.Green -> resources.getColor(R.color.green_400,null)
            NoteColor.Yellow -> resources.getColor(R.color.yellow_400,null)
            NoteColor.Orange -> resources.getColor(R.color.orange_400,null)
            NoteColor.Gray -> resources.getColor(R.color.gray_400,null)
            else -> 0
        }
    }
    fun intToColor(value: Int?, resources: Resources): Int{
        return enumToColor(intToEnum(value),resources)
    }
}
