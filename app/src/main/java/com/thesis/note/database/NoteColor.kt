package com.thesis.note.database

enum class NoteColor(val id:Int){
    Black(0),
    White(1),

    Red(2),
    Pink(3),
    Purple(4),
    Blue(5),
    Cyan(6),
    Teal(7),
    Green(8),
    Yellow(9),
    Orange(10),
    Gray(11),

    RedDark(12),
    PinkDark(13),
    PurpleDark(14),
    BlueDark(15),
    CyanDark(16),
    TealDark(17),
    GreenDark(18),
    YellowDark(19),
    OrangeDark(20),
    GrayDark(21);

    companion object {
        fun fromID(id: Int): NoteColor? = values().firstOrNull { it.id == id }

    }
}

