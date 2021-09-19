package com.thesis.note.database

import androidx.room.TypeConverter
import com.thesis.note.R

class NoteColorConverter{
    @TypeConverter
    fun intToEnum(value: Int?): NoteColor? {
        return if(value == null)
                null
            else
                NoteColor.fromID(value)
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

                NoteColor.RedDark -> R.color.red_600
                NoteColor.PinkDark -> R.color.pink_600
                NoteColor.PurpleDark -> R.color.purple_600
                NoteColor.BlueDark -> R.color.blue_600
                NoteColor.CyanDark -> R.color.cyan_600
                NoteColor.TealDark -> R.color.teal_600
                NoteColor.GreenDark -> R.color.green_600
                NoteColor.YellowDark -> R.color.yellow_600
                NoteColor.OrangeDark -> R.color.orange_600
                NoteColor.GrayDark -> R.color.gray_600

                else -> R.color.purple_700
            }
        }
    }
}
