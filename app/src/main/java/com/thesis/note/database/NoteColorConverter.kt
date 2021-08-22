package com.thesis.note.database

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

    companion object {
        fun enumToColor(value: NoteColor?): Int {
            return when(value){
                NoteColor.Black -> R.color.black1
                NoteColor.White -> R.color.white
                NoteColor.Red -> R.color.red_400
                NoteColor.Pink -> R.color.pink_400
                NoteColor.Purple -> R.color.purple_400
                NoteColor.Blue -> R.color.blue_400
                NoteColor.Cyan -> R.color.cyan_400
                NoteColor.Teal -> R.color.teal_400
                NoteColor.Green -> R.color.green_400
                NoteColor.Yellow -> R.color.yellow_400
                NoteColor.Orange -> R.color.orange_400
                NoteColor.Gray -> R.color.gray_400
                else -> R.color.purple_700
            }
        }
/*
        fun intToColor(value: Int?): Int{
            return enumToColor(intToEnum(value))
        }

 */
    }
}
