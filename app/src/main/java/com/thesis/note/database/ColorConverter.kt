package com.thesis.note.database

import androidx.room.TypeConverter
import com.thesis.note.R

/**
 * Type converter for storing color in database.
 */
class ColorConverter{
    @TypeConverter
    fun intToEnum(value: Int?): Color? {
        return if(value == null)
                null
            else
                Color.fromID(value)
    }

    @TypeConverter
    fun enumToInt(noteType: Color?): Int? {
        return noteType?.id
    }

    companion object {
        /**
         * Get color resource from [Color]
         */
        fun enumToColor(value: Color?): Int {
            return when(value){
                Color.Black -> R.color.black1
                Color.White -> R.color.white

                Color.Red -> R.color.red_400
                Color.Pink -> R.color.pink_400
                Color.Purple -> R.color.purple_400
                Color.Blue -> R.color.blue_400
                Color.Cyan -> R.color.cyan_400
                Color.Teal -> R.color.teal_400
                Color.Green -> R.color.green_400
                Color.Yellow -> R.color.yellow_400
                Color.Orange -> R.color.orange_400
                Color.Gray -> R.color.gray_400

                Color.RedDark -> R.color.red_600
                Color.PinkDark -> R.color.pink_600
                Color.PurpleDark -> R.color.purple_600
                Color.BlueDark -> R.color.blue_600
                Color.CyanDark -> R.color.cyan_600
                Color.TealDark -> R.color.teal_600
                Color.GreenDark -> R.color.green_600
                Color.YellowDark -> R.color.yellow_600
                Color.OrangeDark -> R.color.orange_600
                Color.GrayDark -> R.color.gray_600

                else -> R.color.purple_700
            }
        }
    }
}
